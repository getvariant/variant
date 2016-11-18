package com.variant.server.schema;

import scala.collection.JavaConversions.asScalaBuffer
import org.apache.commons.lang3.time.DurationFormatUtils
import com.variant.core.exception.Error.Severity
import com.variant.core.exception.RuntimeInternalException
import com.variant.core.impl.UserHooker
import com.variant.core.schema.ParserResponse
import com.variant.core.schema.parser.SchemaParser
import play.api.Logger
import com.variant.core.VariantProperties
import com.variant.server.ServerPropertiesKey
import java.io.File
import com.variant.server.ServerErrorException
import com.variant.server.ServerError._
import scala.io.Source
import com.variant.core.schema.parser.ParserResponseImpl

/**
 * 
 */
object SchemaService {
   def apply(hooker: UserHooker, properties: VariantProperties) = 
      new SchemaService(hooker, properties)
}

/**
 * 
 */
class SchemaService (hooker: UserHooker, properties: VariantProperties) {
	         
   private[this] val logger = Logger(this.getClass)
   private[this] val parser = new SchemaParser(hooker)
   
   var deployedSchema: Option[ServerSchema] = None
   
   boot()
   
   /**
    * Boot up schema service.
    */
   def boot() {
      
      var dirName = sys.props.get(ServerPropertiesKey.SCHEMAS_DIR.getExternalName)
      // This will throw an exception if config property is unset.
      if (dirName.isEmpty) dirName = Option(properties.getString(ServerPropertiesKey.SCHEMAS_DIR))
      
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
      else {
         schemaFiles.foreach { file => 
            try parse(file, true)
   		   catch {
   		      case t: Throwable => 
   		         throw new RuntimeInternalException("Unable to parse schema file %s".format(file.getAbsolutePath), t)
   		   }
         }
      }
   }
   
   /**
    * Currently deployed schema.
    */
   def schema = deployedSchema
   
   /**
	 * Parse and optionally deploy if no errors.
	 */
	private def parse(schemaFile: File, deploy: Boolean): ParserResponse = {

      // Read file content into memory string.
      val json = Source.fromFile(schemaFile).mkString
            
      // Parser
      val response = parser.parse(json)
      
		val newSchema = ServerSchema(response.asInstanceOf[ParserResponseImpl].getSchema(), schemaFile)
		
		// Only replace the schema if 1) we were asked to, and 2) no ERROR or higher level errors.
		if (response.hasMessages(Severity.ERROR)) {
			newSchema.setState(State.Failed)
			
			logger.error("Schema %s was not deployed due to parser error(s):".format(newSchema.name))
		}
		else if (deploy) {

			deployedSchema.foreach { _.setState(State.Undeployed) }
			deployedSchema = Some(newSchema)
			deployedSchema.foreach { _.setState(State.Deployed) }
			
			val msg = new StringBuilder();
			msg.append("Deployed schema %s (%s):".format(newSchema.name, newSchema.getId()));
			for (test <- newSchema.getTests()) {
				msg.append("\n   ").append(test.getName()).append(":[");
				var first = true;
				for (exp <- test.getExperiences()) {
					if (first) first = false;
					else msg.append(", ");
					msg.append(exp.getName());
					if (exp.isControl()) msg.append(" (control)");
				}
				msg.append("]");
				msg.append(" (").append(if (test.isOn()) "ON" else "OFF").append(")");
			}
			logger.info(msg.toString());
		}
		
		response
	}
   
}
