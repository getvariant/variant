package com.variant.server.test.spec

import play.api.libs.json.JsValue
import com.variant.core.ServerError
import org.scalatestplus.play.PlaySpec
import com.variant.server.api.Session
import com.variant.core.session.SessionScopedTargetingStabile
import com.variant.server.impl.SessionImpl
import com.variant.core.schema.Schema
import com.variant.core.util.StringUtils
import com.variant.core.util.Constants._
import java.util.Random
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.libs.json.Json
import play.api.Application
import com.variant.server.boot.VariantServer
import com.variant.server.conn.ConnectionStore
import com.variant.server.conn.SessionStore
import scala.concurrent.Future
import play.api.mvc._
import com.variant.core.ConnectionStatus

/**
 * 
 */
trait BaseSpec extends PlaySpec {
      
   private val rand = new Random(System.currentTimeMillis)
   /**
    * Upstream will define the application.
    * note, that its value is not stable, so dependencies
    * should be defs.
    */
   protected def application: Application
   
   protected def context = application.configuration.get[String]("play.http.context")
   protected def server = application.injector.instanceOf[VariantServer]

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
      def withConnStatusHeader(expected: ConnectionStatus*) = {
         header(HTTP_HEADER_CONN_STATUS, res) match {
            case Some(h) => {
               val act = ConnectionStatus.valueOf(h)
               if (!expected.contains(act)) fail {
                  s"Connection status ${act} in header was not equal ${expected.mkString(",")} " + stackLine
               }
            }
            case None => {
               fail {
                  s"No connection status in header was not equal ${expected.mkString(",")} " + stackLine
               }
            }
         }
         this
      }
      
      /**
       * 
       */
      def withNoConnStatusHeader = {
         header(HTTP_HEADER_CONN_STATUS, res) mustBe None
         this
      }

      /**
       * 
       */
      def withNoBody = {
         if (contentAsString(res) == Some)
            fail { "Response body was not empty " + stackLine }
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
               fail(t.getMessage + stackLine) 
         }
         
         this
      }

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
		schema.getTest(tokens(0)).getExperience(tokens(1))
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
    * All connect route calls should use this. This is the only request
    * that does not have connection id in X-Connection-ID header.
    */
   protected def connectionRequest(schemaName: String) = {
      FakeRequest(POST, context + "/connection/" + schemaName).withHeaders("Content-Type" -> HTTP_HEADER_CONTENT_TYPE)
   }
   
   /**
    * All route calls, emulating a connected API call.
    */
   protected def connectedRequest(method: String, uri: String, cid: String) = {
      FakeRequest(method, uri)
         .withHeaders("Content-Type" -> HTTP_HEADER_CONTENT_TYPE, HTTP_HEADER_CONNID -> cid)
   }

}