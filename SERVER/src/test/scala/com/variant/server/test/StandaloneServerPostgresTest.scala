package com.variant.server.test

import java.io.PrintWriter

import scala.sys.process._

import com.variant.core.util.StringUtils
import com.variant.core.error.ServerError._

import com.variant.server.boot.VariantServer
import com.variant.server.boot.ServerMessageLocal._
import com.variant.server.test.spec.StandaloneServerSpec
import com.variant.server.test.util.ServerLogTailer
import com.variant.server.test.util.ServerLogTailer.Level._
import com.variant.core.httpc.HttpRequest
import com.variant.core.httpc.HttpStatusCode._
import com.variant.server.boot.ServerMessageLocal
import com.variant.core.httpc.HttpMethod
import com.variant.server.test.routes.SessionTest
import play.api.libs.json.Json
import com.variant.core.session.CoreSession
import com.variant.core.schema.parser.SchemaParser
import com.variant.server.schema.ServerSchemaParser
import com.variant.core.schema.parser.SchemaParserServerless
import com.variant.server.test.spec.Async
import com.variant.core.schema.State
import com.variant.core.schema.Schema
import scala.util.Random
import com.variant.core.httpc.HttpResponse

/**
 * Test the server running in a separate process.
 */
class StandaloneServerPostgresTest extends StandaloneServerSpec with Async {

   override lazy val flusher = "postgres"

   "Server" should {
/*
      "send NOT_FOUND on a bad request" in {

         // This is a mystery: None of these 3 lines actually throws any exceptions.
         // Moreover, this test file runs fine (with `testOnly`) without the try block
         // But the full harness (with `test`) fails without the try block with weird socket
         // exceptions. Mystery.
         try {
            HttpRequest.get("http://localhost:5377/foo")
               .responseCode mustBe HTTP_NOT_FOUND
         } catch {
            case t: Throwable => println("************** " + t.getMessage)
         }
      }

      "send health on a root request" in {
         val resp = HttpRequest.get("http://localhost:5377")
         resp.responseCode mustBe HTTP_OK
         resp.bodyString.get must startWith(VariantServer.productVersion._1)
      }

      "fail to deploy without the right JDBC driver in ext/" in {

         server.stop()
         s"rm ${serverDir}/ext/postgresql-42.2.5.jar".!!
         server.start()

         val resp1 = HttpRequest.get("http://localhost:5377/schema/monstrosity")
         resp1.responseCode mustBe HTTP_BAD_REQUEST
         resp1.getErrorContent mustBe UNKNOWN_SCHEMA.asMessage("monstrosity")

         val lines = ServerLogTailer.last(3, serverDir + "/log/variant.log")
         lines(0).level mustBe Error
         lines(0).message mustBe OBJECT_INSTANTIATION_ERROR.asMessage("com.variant.extapi.std.flush.jdbc.TraceEventFlusherPostgres", "java.lang.reflect.InvocationTargetException")
         lines(1).level mustBe Warn
         lines(1).message mustBe SCHEMA_FAILED.asMessage("exampleSchema", s"${serverDir}/schemata/example.schema")
         lines(2).level mustBe Info
         lines(2).message must include regex s"\\[${ServerMessageLocal.SERVER_BOOT_OK.getCode}\\].*started on port"

      }

      "Redeploy example schema after restoring the JDBC driver in ext/" in {

         // Hot redeploy won't work. Not sure why, prob. class loader will refuse to look for the same class twice.
         server.stop()

         // Our current directory at test runtime is /test-base
         s"cp ../standalone-server/ext/postgresql-42.2.5.jar ${serverDir}/ext/" !!;

         server.start()

         HttpRequest.get("http://localhost:5377/schema/exampleSchema").responseCode mustBe HTTP_OK

      }
*/
      "Not lose trace events on shutdown" in {
         
         server.stop()

         server.start()

         val sessions = 2
         val hops = 2
         
         val specialKey = "specialKey"
         val specialVal = "This is how we'll be able to tell the events we're about to insert from the ones that are already there"
         
         var count = new java.util.concurrent.atomic.AtomicInteger(0)         
         for (i <- 0 until sessions) async {
            val (schema, sid) = createSession("exampleSchema")
            val state1 = schema.getState("state1").get
            for (j <- 0 until hops) async {
               targetSessionForState(sid, state1)
               // A bit of a delay so as not to overwhelm the buffer cache.
               Thread.sleep(200 + Random.nextInt(600))
            }
         }
         
         joinAll(timeout = 60000)

         server.stop()
         val lines = ServerLogTailer.last(3, serverDir + "/log/variant.log")
         println(lines)
      }

   }
   
   /**
    * Create a new session in a given schema
    */
   private def createSession(schemaName: String): (Schema, String) = {
      
      val resp = HttpRequest.post(s"http://localhost:5377/session/${schemaName}/foo", SessionTest.emptyTargetingTrackerBody)

      if (resp.responseCode != HTTP_OK) {
         fail(s"Unexpeted HTTP response code ${resp.responseCode} [${resp.bodyString}]")          
      }
      val bodyJson = Json.parse(resp.bodyString.get)
      val schema = new SchemaParserServerless().parse((bodyJson \ "schema" \ "src").as[String]).getSchema
      schema.getMeta.getName mustBe schemaName
      val ssn = CoreSession.fromJson((bodyJson \ "session").as[String], schema)
      (schema, ssn.getId)
   }

   /**
    * Create a new session in a given schema
    */
   private def targetSessionForState(sid: String, state: State) {
      
      val body = s"""{"state":"${state.getName}"}"""
      val resp = HttpRequest.post(s"http://localhost:5377/request/${state.getSchema.getMeta.getName}/${sid}", body)
      if (resp.responseCode != HTTP_OK) {
         fail(s"Unexpeted HTTP response code ${resp.responseCode} [${resp.bodyString}]")          
      }
   }

   /**
    * Cleanup.
    */
   override def afterAll() {
      server.stop()
   }

}
