package com.variant.server.test.spec

import java.util.Random
import scala.concurrent.Future
import org.scalatestplus.play.PlaySpec
import com.variant.core.error.ServerError
import com.variant.core.error.UserError
import com.variant.core.schema.Schema
import com.variant.core.session.SessionScopedTargetingStabile
import com.variant.core.Constants.HTTP_HEADER_CONTENT_TYPE
import com.variant.core.util.StringUtils
import com.variant.server.impl.SessionImpl
import com.variant.server.boot.VariantServer
import com.variant.server.impl.SessionImpl
import play.api.Application
import play.api.libs.json._
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import com.variant.server.schema.ServerSchemaParser
import com.variant.server.schema.SchemaGen
import com.variant.server.api.Session

/**
 * Base server spec
 */
trait BaseServerSpec extends PlaySpec {
      
   private val rand = new Random(System.currentTimeMillis)
   /**
    * Upstream will define the application.
    * note, that its value is not stable, so dependencies
    * should be defs.
    */
   protected def application: Application
   
   protected def server = application.injector.instanceOf[VariantServer]

   protected def liveGenOf(schemaName: String) = server.schemata.get(schemaName).get.liveGen.get
   
   protected def assertResp(resp: Option[Future[Result]]) = {
      new ResultWrap(resp.get)
   }

   /**
    * Response wrapper class used by assertResp()
    */
   protected class ResultWrap(val res: Future[Result]) {

      val actCode = status(res)
      val actError: Option[(Boolean, ServerError, Seq[String])] =
         if (actCode == BAD_REQUEST)  Some(parseError(contentAsJson(res)))
         else None
          
    /**
     * Print the stack line that's just below this class.
     */
     private[this] def stackLine = {
        val stackLine = {
           val stackTrace = Thread.currentThread().getStackTrace
           var found = false
           var i = 3
           var line = stackTrace.apply(i)
           while (!found) {
              if (line.getClassName.contains(this.getClass.getSimpleName)) {
                 i += 1
                 line = stackTrace.apply(i)
              }
              else {
                 found = true
              }       
           }
           line
        }
        " (" + stackLine.getFileName + " " + stackLine.getLineNumber + ")"            
     }
      
     /**
       * 
       */
      def is(code: Int): ResultWrap = {         
         if (actCode != code) {
            fail {
               s"Status ${actCode} was not equal ${code}" + {
                  if (actCode == BAD_REQUEST) {
                     val (isInternal, error, args) = actError.get
                     "\n  Unexpected error " + error.asMessage(args:_*)
                  }
                  else ""
               } + stackLine
            }
         }                                 
         this
      }

      def echo(): ResultWrap = {
         println("*** ECHO RESULT *** \n" + status(res) + ", body: '" + contentAsString(res) + "'")
         this
      }
     /**
       * 
       */
      def isOk: ResultWrap = {         
         is(OK)
      }
      
      def isError(error: ServerError, args: String*) = {
         if (actCode == BAD_REQUEST) {
            val msg = error.asMessage(args:_*)
            val actMsg = actError.get._2.asMessage(actError.get._3:_*)
            if (actMsg != msg)
               fail {
                  s"Error [${actMsg}] was not equal [${msg}] " + stackLine 
            }
         }
         else 
            fail {
               s"Status ${actCode} was not 400 " + stackLine  
            }
         this
      }      

      /**
       * 
       */
      def withNoBody = {
         val body = contentAsString(res)
         if (body != null && body.length() > 0)
            fail { s"Response body [$body] was not empty " + stackLine }
         this
      }

      /**
       * 
       */
      def withBody(content: String) = {
         contentAsString(res) mustBe content
         this
      }

      /**
       * 
       */
      def withBodyText(func: String => Unit) = {
         func(contentAsString(res))
         this
      }

      /**
       * 
       */
      def withBodyJson(func: JsValue => Unit) = {
         try {
            func(contentAsJson(res)) 
         } catch {
            case t: Throwable => 
               fail(t.getMessage + stackLine, t) 
         }
         
         this
      }

      /**
       * Deserialize session from response body.
       * Extract session ID from the json and use that
       * to obtain the schema gen for deserialization.
       */
      def withBodySession(ssnFunc: (SessionImpl) => Unit = {ssn => }) = {

         try {
            val json = contentAsJson(res)
            val ssnJson = Json.parse((json \ "session").as[String])
            val sid = (ssnJson \ "sid").as[String]
            val gen = server.ssnStore.get(sid).get.schemaGen
            val ssn = SessionImpl((json \ "session").as[String], gen);
            ssnFunc(ssn)
         } catch {
            case t: Throwable => 
               fail(t.getMessage + stackLine, t) 
         }

         this
      }

      /**
       * Extract the value of an attribute from the response JSON.
       * Note that the session JSON is stringified, so has to be parsed.
       * THIS DOESN'T QUITE WORK
      def withBodySession
         (schemaName: String)
         (ssnFunc: (SessionImpl) => Unit = {ssn => })
         (returnsFunc: JsValue => Unit = {json => }) = {

         try {
            val liveGen = server.schemata.getLiveGen(schemaName).get
            val json = contentAsJson(res)
            val ssn = SessionImpl((json \ "session").as[String], liveGen);
            ssnFunc(ssn)
            returnsFunc((json \ "session").as[JsValue])
         } catch {
            case t: Throwable => 
               fail(t.getMessage + stackLine, t) 
         }

         this
      }
      * 
      */
   }      

   /**
    * Parse an 400 error body
    */
   protected def parseError(body: JsValue): (Boolean, ServerError, Seq[String]) = {
      body mustNot be (null)
      val isInternal = (body \ "isInternal").as[Boolean]
      val code = (body \ "code").as[Int] 
      val args = (body \ "args").as[Seq[String]]
      (isInternal, ServerError.byCode(code), args)
   }
   
   /**
    * Create and add a targeting stabile to a session.
    */
   protected def setTargetingStabile(ssn: Session, experiences: String*) {
		val stabile = new SessionScopedTargetingStabile()
		experiences.foreach {e => stabile.add(experience(e, ssn.getSchema))}
		ssn.asInstanceOf[SessionImpl].coreSession.setTargetingStabile(stabile);
	}

   /**
    * Find experience object by its comma separated name.
    */
   protected def experience(name: String, schema: Schema) = {
		val tokens = name.split("\\.")
		assert(tokens.length == 2)
		schema.getVariation(tokens(0)).get.getExperience(tokens(1)).get
	}

   /**
    * Generate a new random session ID.
    */
   protected def newSid() = 
      StringUtils.random64BitString(rand)
   
   /**
    * Normalize a JSON string by removing any white space.
    */
   protected def normalJson(jsonIn: String):String = Json.stringify(Json.parse(jsonIn))
   
   /**
    * All route calls, emulating an API call.
    */
   protected def httpReq(method: String, uri: String) =
      FakeRequest(method, uri)
         .withHeaders("Content-Type" -> HTTP_HEADER_CONTENT_TYPE)

}