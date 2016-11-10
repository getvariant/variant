package com.variant.server.schema;

import scala.collection.JavaConversions.asScalaBuffer
import org.apache.commons.lang3.time.DurationFormatUtils
import com.variant.core.exception.Error.Severity
import com.variant.core.exception.RuntimeInternalException
import com.variant.core.impl.UserHooker
import com.variant.core.schema.ParserResponse
import com.variant.core.schema.parser.SchemaParser
import com.variant.core.schema.parser.ParserResponseImpl
import play.api.Logger
import com.variant.core.VariantProperties
import com.variant.server.ServerPropertiesKey
import java.io.File
import com.variant.server.ServerErrorException
import com.variant.server.ServerError._
import scala.io.Source

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

      val schemas = dir.listFiles()
      
      if (schemas.length == 0) {
         logger.info("No schemas detected")
      }
      else if (schemas.length > 1)  
         throw new ServerErrorException(MULTIPLE_SCHEMAS_NOT_SUPPORTED)
      else {
         val schemaParser = new SchemaParser(hooker)
         val fileContent = Source.fromFile(schemas(0)).mkString
         val json = parser.preParse(fileContent)
         val response = parser.parse(json)
         println("********************** " + response.getMessages)
      }
   }
   
   /**
    * Currently deployed schema.
    */
   def schema = deployedSchema
   
   /**
	 * Parse and optionally deploy if no errors.
	 */
	private def parse(rawJson: String, deploy: Boolean): ParserResponse = {

		val now = System.currentTimeMillis();
		lazy val response: ParserResponse = null
		
		try {
			val response = parser.parse(rawJson)
		}
		catch {
		   case t: Throwable => throw new RuntimeInternalException(t)
		}

		val newSchema = ServerSchema(response.asInstanceOf[ParserResponseImpl].getSchema())
		
		// Only replace the schema if 1) we were asked to, and 2) no ERROR or higher level errors.
		if (!response.hasMessages(Severity.ERROR)) {
			newSchema.setState(State.Failed)
			logger.error("New schema was not deployed due to parser error(s).");
		}
		else if (deploy) {
			
			deployedSchema.foreach { _.setState(State.Undeployed) }
			deployedSchema = Some(newSchema)
			deployedSchema.foreach { _.setState(State.Deployed) }
			
			val msg = new StringBuilder();
			msg.append("New schema ID [").append(newSchema.getId()).append("] deployed in ");
			msg.append(DurationFormatUtils.formatDuration(System.currentTimeMillis() - now, "mm:ss.SSS")).append(":");
			for (test <- newSchema.getTests()) {
				msg.append("\n   ").append(test.getName()).append(" {");
				var first = true;
				for (exp <- test.getExperiences()) {
					if (first) first = false;
					else msg.append(", ");
					msg.append(exp.getName());
					if (exp.isControl()) msg.append(" (control)");
				}
				msg.append("}");
				if (!test.isOn()) msg.append(" OFF");
			}
			logger.info(msg.toString());
		}
		
		response
	}
   
}
