package com.variant.server.schema;

import java.io.File
import scala.collection.JavaConversions.asScalaBuffer
import scala.io.Source
import com.variant.core.UserError.Severity
import com.variant.core.exception.InternalException
import com.variant.core.schema.ParserMessage
import com.variant.core.schema.parser.SchemaParser
import com.variant.server.boot.ServerErrorLocal._
import com.variant.server.ConfigKeys._
import com.variant.server.boot.VariantServer.server
import play.api.Logger
import com.variant.core.schema.ParserResponse
import org.apache.commons.io.IOUtils
import com.variant.core.UserErrorException

trait SchemaDeployer {
 
   /**
    * Deploy the schema represented by this schema deployer
    */
   def deploy: ParserResponse
   def schema: Option[ServerSchema]
}

object SchemaDeployer {

   /**
    * Deploy from the file system
    */
   def fromFileSystem() = new SchemaDeployerFromFS()

   /**
    * Deploy from a memory string
    */
   def fromString(schemaSrc: String) = new SchemaDeployerFromString(schemaSrc)
   
  /**
    * Deploy from a classpath resource
    */
   def fromClasspath(resource: String) = new SchemaDeployerFromClasspath(resource)

}

abstract class AbstractSchemaDeployer() extends SchemaDeployer {
   
   private val logger = Logger(this.getClass)  
   var deployedSchema: Option[ServerSchema] = None

   override def schema = deployedSchema
   
   /**
	 * Parse a schema.
	 */
	protected def parseAndDeploy(schemaSrc: String): ParserResponse = {
            
      val parser = new SchemaParser(server.hooker)
      val response = parser.parse(schemaSrc)
		val schema = ServerSchema(response)		
		if (response.hasMessages(Severity.ERROR)) schema.state = State.Failed
      
		if (response.hasMessages(Severity.ERROR)) {
			logger.error("Schema was not deployed due to parser error(s):")
			response.getMessages(Severity.ERROR).foreach {log(_)}
		}
		else {
   		deployedSchema.foreach { _.state = State.Undeployed }
   		deployedSchema = Some(schema)
   		deployedSchema.foreach { _.state = State.Deployed }
   		
   		val msg = new StringBuilder()
   		msg.append("Deployed schema [%s], ID [%s]:".format(schema.getName, schema.getId));
   		for (test <- schema.getTests) {
   			msg.append("\n   ").append(test.getName()).append(":[");
   			var first = true;
   			for (exp <- test.getExperiences()) {
   				if (first) first = false;
   				else msg.append(", ");
   				msg.append(exp.getName);
   				if (exp.isControl) msg.append(" (control)");
   			}
   			msg.append("]");
   			msg.append(" (").append(if (test.isOn()) "ON" else "OFF").append(")");
   		}
   		
   		logger.info(msg.toString())
   		
   		if (response.hasMessages()) {
   			logger.info("Schema deployed with non-fatal messages:".format(schema.getName))
      		response.getMessages().foreach {log(_)}
   		}
		}
      response
	}
   
   /**
    * 
    */
   private def log(msg: ParserMessage) {
      val sep = " "
      val result = new StringBuilder()
      result.append(msg.getCode)
      if (msg.getLine != null) {
         result.append(sep)
         result.append("[line ").append(msg.getLine).append(", col ").append(msg.getColumn).append("]")
      }   
      result.append(sep).append(msg.getText)
      
      msg.getSeverity match {
         case Severity.FATAL | Severity.ERROR => logger.error(result.toString())
         case Severity.WARN => logger.warn(result.toString())
         case Severity.INFO => logger.info(result.toString())
      }
   }
}

/**
 * File System based schema service implementation
 *
object SchemaDeployerFromFS {
   def apply() = new SchemaDeployerFromFS()
}
*/
/**
 * Deploy schemas from a directory on the file system.
 * Multiple schema files in directory are not yet supported,
 * user error will be thrown if number of files in
 * ServerPropertiesKey.SCHEMAS_DIR is other than 1.
 */
class SchemaDeployerFromFS() extends AbstractSchemaDeployer {
	         
   private val logger = Logger(this.getClass)
   
   /**
    * Read content of schemas dir and deploy.
    */
   override def deploy() = {

      var dirName = sys.props.get(SCHEMAS_DIR)

      // This will throw an exception if config property is unset.
      if (dirName.isEmpty) dirName = Option(server.config.getString(SCHEMAS_DIR))
      
      val dir = new File(dirName.get)
      if (!dir.exists) 
         throw new UserErrorException(SCHEMAS_DIR_MISSING, dirName.get)
      if (!dir.isDirectory) 
         throw new UserErrorException(SCHEMAS_DIR_NOT_DIR, dirName.get)            
   
      val schemaFiles = dir.listFiles()
      
      if (schemaFiles.length == 0) {
         logger.info("No schemas detected")
      }
      else if (schemaFiles.length > 1)  
         throw new UserErrorException(MULTIPLE_SCHEMAS_NOT_SUPPORTED, dirName.get)
   
      val schemaFile = schemaFiles.head
      parseAndDeploy(Source.fromFile(schemaFile).mkString)
   }
}

/**
 * Deploy single schema from a string in memory.
 */
class SchemaDeployerFromString(schemaSrc: String) extends AbstractSchemaDeployer {
	         
   override def deploy() = parseAndDeploy(schemaSrc)
}

/**
 * Deploy single schema from classpath. Uses resource name as schema name;
 */
class SchemaDeployerFromClasspath(resource: String) extends AbstractSchemaDeployer {
	         
   val stream = getClass.getResourceAsStream(resource)
   if (stream == null)
      throw new InternalException("Unable to open classpath resource [%s]".format(resource))
   
   override def deploy() = parseAndDeploy(IOUtils.toString(stream))
}

