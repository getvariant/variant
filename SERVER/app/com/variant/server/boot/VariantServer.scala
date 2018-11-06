package com.variant.server.boot

import scala.collection.JavaConversions.asScalaSet
import scala.collection.mutable
import scala.concurrent.Future
import org.apache.commons.lang3.time.DurationFormatUtils
import com.typesafe.config.Config
import com.variant.core.UserError.Severity
import com.variant.server.api.ServerException
import com.variant.server.schema.SchemaDeployer
import com.variant.server.schema.Schemata
import javax.inject.Inject
import javax.inject.Singleton
import play.api.Configuration
import play.api.Logger
import play.api.inject.ApplicationLifecycle
import java.lang.management.ManagementFactory


/**
 * Need a trait to make DI to work.
 */
trait VariantServer {
   
   val isUp: Boolean 
   val config: Config // Do not expose Play's Configuration
   //val classloader: VariantClassLoader
   val startupErrorLog = mutable.ArrayBuffer[ServerException]()
   // Read-only snapshot
   def schemata: Schemata
   val ssnStore: SessionStore
   def useSchemaDeployer(newDeployer: SchemaDeployer): Unit
   def schemaDeployer: SchemaDeployer
}

/**
 * 
 */
object VariantServer {

   val version = "0.9.3"
   val productName = "Variant Experience Server release %s".format(version)

   // Static instance location.
   private [boot] var _instance: VariantServer = _
   // This must be a method because some tests will rebuild the server, so the content of _instance will be changing.
   def instance = _instance
 
}

/**
 * Instantiated once by Play at startup via Guice.
 */
@Singleton
class VariantServerImpl @Inject() (
      playConfig: Configuration, 
      lifecycle: ApplicationLifecycle
      // We ask for the provider instead of the application, because application itself won't be available until
      // all eager singletons are constructed, including this class.
      //application: Application
   ) extends VariantServer {
   
   import VariantServer._
   
	private[this] val logger = Logger(this.getClass)
   private[this] var _schemaDeployer: SchemaDeployer = null
   private[this] var _isUp = false
   private[this] var _ssnStore: SessionStore = null
   private[this] var _vacThread: VacuumThread = null
  
	// Make this instance statically discoverable
	_instance = this	

	override lazy val isUp = _isUp
	
	override val config = playConfig.underlying
	
	//override val classloader = new VariantClassLoader()
   
   override lazy val ssnStore  = _ssnStore
  
	override def schemata = _schemaDeployer.schemata 
	
	override def schemaDeployer = _schemaDeployer
		
   bootup()

	/**
	 * Application startup.
	 */
   def bootup() {
      
      try {
         // Echo all config keys if debug
      	if (logger.isDebugEnabled) {
           config.entrySet().filter(x => x.getKey.startsWith("variant.")).foreach(e => logger.debug("  %s => [%s]".format(e.getKey, e.getValue())))
         }
            	
      	
      	_ssnStore = new SessionStore(this)

      	// Default schema deployer is from file system, but may be overridden by tests.
         useSchemaDeployer(SchemaDeployer.fromFileSystem())

       	_vacThread = new VacuumThread(this)
      	_vacThread.start()

      }
      catch {
         case se: ServerException => startupErrorLog += se
         case e: Throwable => throw new RuntimeException("Uncaught exception", e) // This will be uncaught and crash the server.
      }
      
      if (startupErrorLog.exists { _.getSeverity == Severity.FATAL }) {
   		logger.error(ServerErrorLocal.SERVER_BOOT_FAILED.asMessage(productName))
   		// Only print error stack if internal error.
   		startupErrorLog.foreach { e => 
   		   if (e.isInstanceOf[ServerExceptionInternal])
      		   logger.error(e.getMessage(), e) 
      		else
      		   logger.error(e.getMessage())
   	   }
   	   shutdown()
   	   
   	}
   	else {
         logger.info(ServerErrorLocal.SERVER_BOOT_OK.asMessage(
               productName,
               config.getString("http.port"),
               config.getString("play.http.context"),
      			DurationFormatUtils.formatDuration(ManagementFactory.getRuntimeMXBean().getUptime(), "mm:ss.SSS")))   	

         _isUp = true
   	}      

   }

  /**
	 * Tests can override the default schema deployer to be able to deploy from a memory string.
	 */
   override def useSchemaDeployer (newDeployer: SchemaDeployer): Unit = {
      _schemaDeployer = newDeployer
   	_schemaDeployer.bootstrap()
	 }
   
   /**
    * Application shutdown hook called by Play.
    */
   def shutdown() {
      
      logger.info(s"${productName} is shutting down")
      
      // Undeploy all schemata
      schemata.undeployAll()
      
      logger.info(ServerErrorLocal.SERVER_SHUTDOWN.asMessage(
            productName,
            config.getString("http.port"),
            config.getString("play.http.context"),
   			DurationFormatUtils.formatDuration(ManagementFactory.getRuntimeMXBean().getUptime(), "HH:mm:ss")))
   			   			
      // System.exit(0)  << DO NOT DO THIS! Messes up tests.
   	// Ideally, we want to exit in Prod to make the application quit and come back to the
   	// OS prompt here, while simply return in Test, in order not to crash the test executor.
   	// Figuring out the Mode seems tricky as I keep running into circular dependency in DI.
   	// Perhaps not worth wasting time on, because we should drop Play in favor of direct Akka HTTP anyway.

   }
   
  /** When the application starts, register a stop hook with the
    * ApplicationLifecycle object. The code inside the stop hook will
    * be run when the application stops.
    */
   lifecycle.addStopHook { () =>
       shutdown()
       Future.successful(())
   }

}
