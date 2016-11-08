package com.variant.server.schema;

import scala.collection.JavaConversions._
import org.apache.commons.lang3.time.DurationFormatUtils
import com.variant.core.exception.Error.Severity
import com.variant.core.schema.ParserMessage
import com.variant.core.schema.ParserResponse
import com.variant.core.schema.Test
import com.variant.core.schema.Test.Experience
import com.variant.core.schema.impl.SchemaImpl
import com.variant.core.schema.parser.SchemaParser
import com.variant.core.impl.UserHooker
import com.variant.core.exception.RuntimeInternalException
import play.api.Logger
import com.variant.core.schema.parser.ParserResponseImpl

/**
 * 
 */
object SchemaService {
   def apply(hooker: UserHooker) = new SchemaService(hooker)
}

/**
 * 
 */
class SchemaService (hooker: UserHooker) {
	      
   private val logger = Logger(this.getClass)
   val schemaParser = new SchemaParser(hooker)	
   var deployedSchema: Option[ServerSchema] = None

   /**
    * Currently deployed schema.
    */
   def schema() = deployedSchema
   
  /**
	 * Parse and deploy if no errors.
	 */
	def parse(schema: String): ParserResponse = {
		parse(schema, true)
	}

   /**
	 * Parse and optionally deploy if no errors.
	 */
	def parse(rawJson: String, deploy: Boolean): ParserResponse = {

		val now = System.currentTimeMillis();
		
		lazy val response: ParserResponse = null
		
		try {
			val response = schemaParser.parse(rawJson)
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
