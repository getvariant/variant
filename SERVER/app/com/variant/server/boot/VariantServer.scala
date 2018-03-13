package com.variant.server.boot

import scala.concurrent.Future
import scala.collection.JavaConversions._
import scala.collection.mutable
import org.apache.commons.lang3.time.DurationFormatUtils
import com.variant.server.event.EventWriter
import javax.inject._
import javax.inject.Singleton
import play.api.Configuration
import play.api.Logger
import play.api.inject.ApplicationLifecycle
import play.api.routing.Router
import com.variant.core.schema.Schema
import com.variant.server.schema.ServerSchema
import play.api.Application
import com.variant.core.schema.parser.ParserResponse
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.variant.core.UserError.Severity._
import com.variant.server.api.ServerException
import play.api.ApplicationLoader
import com.variant.server.schema.SchemaDeployer
import com.variant.server.util.VariantClassLoader
import com.variant.core.RuntimeError
import com.variant.core.UserError.Severity


/**
 * Need a trait to make DI to work.
 */
trait VariantServer {
   
   val config: Config // Do not expose Play's Configuration
   val classloader: VariantClassLoader
   val startupErrorLog = mutable.ArrayBuffer[ServerException]()
   val productName = "Variant Experiment Server release %s".format(SbtService.version)
   val startTs = System.currentTimeMillis
   def schemata: Map[String, ServerSchema]
   def useSchemaDeployer(newDeployer: SchemaDeployer): Unit
   def isUp: Boolean
   def schemaDeployer: SchemaDeployer
}

/**
 * 
 */
object VariantServer {

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
      //appProvider: Provider[Application] 
   ) extends VariantServer {
   
   import VariantServer._
   
	private[this] val logger = Logger(this.getClass)
   private[this] var _schemaDeployer: SchemaDeployer = null
  
	// Make this instance statically discoverable
	_instance = this	
	
	override val config = playConfig.underlying
	
	override val classloader = new VariantClassLoader()
  
	override def schemata = _schemaDeployer.schemata 
	
	override def schemaDeployer = _schemaDeployer
	
   override def isUp = ! startupErrorLog.exists { _.getSeverity == Severity.FATAL }
	
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
      
      	// Default schema deployer is from file system, but may be overridden by tests.
         useSchemaDeployer(SchemaDeployer.fromFileSystem())
      }
      catch {
         case se: ServerException => startupErrorLog += se
         case e: Throwable => throw new RuntimeException("Uncaught exception", e) // This will be uncaught and crash the server.
      }
      
      if (isUp) {
         logger.info("%s bootstrapped on :%s%s in %s.".format(
               productName,
               config.getString("http.port"),
               config.getString("play.http.context"),
      			DurationFormatUtils.formatDuration(System.currentTimeMillis() - startTs, "mm:ss.SSS")))   	
   	}
   	else {
   		logger.error("%s failed to bootstrap due to following ERRORS:".format(productName))
   		startupErrorLog.foreach { e => logger.error(e.getMessage(), e) }
   	   shutdown()
   	   //System.exit(0)
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
      
      // Undeploy all schemata
      schemata.foreach { case (name: String, schema: ServerSchema) => schema.undeploy() }
      
      logger.info("%s shutdown on :%s%s. Uptime %s.".format(
            productName,
            config.getString("http.port"),
            config.getString("play.http.context"),
   			DurationFormatUtils.formatDuration(System.currentTimeMillis() - startTs, "HH:mm:ss")))
   }
   
   // When the application starts, register a stop hook with the
   // ApplicationLifecycle object. The code inside the stop hook will
   // be run when the application stops.
   lifecycle.addStopHook { () =>
       shutdown()
       Future.successful(())
   }

}
