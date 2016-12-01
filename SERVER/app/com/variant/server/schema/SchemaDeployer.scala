package com.variant.server.schema;

import java.io.File
import scala.collection.JavaConversions.asScalaBuffer
import scala.io.Source
import com.variant.core.exception.Error.Severity
import com.variant.core.exception.RuntimeInternalException
import com.variant.core.schema.ParserMessage
import com.variant.core.schema.parser.SchemaParser
import com.variant.server.ServerError.MULTIPLE_SCHEMAS_NOT_SUPPORTED
import com.variant.server.ServerError.SCHEMAS_DIR_MISSING
import com.variant.server.ServerError.SCHEMAS_DIR_NOT_DIR
import com.variant.server.ServerErrorException
import com.variant.server.ServerPropertiesKey
import com.variant.server.boot.VariantServer.server
import play.api.Logger
import com.variant.core.schema.ParserResponse
import org.apache.commons.io.IOUtils

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
   def fromString(name: String, body: String) = new SchemaDeployerFromString(name, body)
   
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
	protected def parseAndDeploy(name: String, body: String): ParserResponse = {
            
      val parser = new SchemaParser(server.hooker)
      val response = parser.parse(body)
		val schema = ServerSchema(name, response.getSchema)		
		if (response.hasMessages(Severity.ERROR)) schema.state = State.Failed
      
		if (response.hasMessages(Severity.ERROR)) {
			logger.error("Schema %s was not deployed due to parser error(s):".format(schema.name))
			response.getMessages(Severity.ERROR).foreach {log(_)}
		}
		else {
   		deployedSchema.foreach { _.state = State.Undeployed }
   		deployedSchema = Some(schema)
   		deployedSchema.foreach { _.state = State.Deployed }
   		
   		val msg = new StringBuilder()
   		msg.append("Deployed schema [%s], ID [%s]:".format(schema.name, schema.getId));
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
   		
   		if (response.hasMessages(Severity.ERROR)) {
   			logger.info("Schema deployed with non-fatal messages:".format(schema.name))
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
   var dirName = sys.props.get(ServerPropertiesKey.SCHEMAS_DIR.getExternalName)

   // This will throw an exception if config property is unset.
   if (dirName.isEmpty) dirName = Option(server.properties.getString(ServerPropertiesKey.SCHEMAS_DIR))
   
   val dir = new File(dirName.get)
   if (!dir.exists) 
      throw new ServerErrorException(SCHEMAS_DIR_MISSING, dirName)
   if (!dir.isDirectory) 
      throw new ServerErrorException(SCHEMAS_DIR_NOT_DIR, dirName)            

   val schemaFiles = dir.listFiles()
   
   if (schemaFiles.length == 0) {
      logger.info("No schemas detected")
   }
   else if (schemaFiles.length > 1)  
      throw new ServerErrorException(MULTIPLE_SCHEMAS_NOT_SUPPORTED)

   val schemaFile = schemaFiles.head
   
   override def deploy() = parseAndDeploy(schemaFile.getName, Source.fromFile(schemaFile).mkString)
}

/**
 * Deploy single schema from a string in memory.
 */
class SchemaDeployerFromString(name: String, body: String) extends AbstractSchemaDeployer {
	         
   override def deploy() = parseAndDeploy(name, body)
}

/**
 * Deploy single schema from classpath. Uses resource name as schema name;
 */
class SchemaDeployerFromClasspath(resource: String) extends AbstractSchemaDeployer {
	         
   val stream = getClass.getResourceAsStream(resource)
   if (stream == null)
      throw new RuntimeInternalException("Unable to open classpath resource [%s]".format(resource))
   val name = resource.substring(1, resource.length() - 5)   // drop leading slash and tailing '.json'
   override def deploy() = parseAndDeploy(name, IOUtils.toString(stream))
}

