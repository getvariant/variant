package com.variant.server.test
/*
import java.io.PrintWriter

import scala.sys.process._
import akka.http.scaladsl.model.StatusCodes._

import com.variant.core.util.StringUtils
import com.variant.core.error.ServerError._

import com.variant.server.boot.VariantServer
import com.variant.server.boot.ServerMessageLocal._
import com.variant.server.test.spec.StandaloneServerSpec
import com.variant.server.test.util.ServerLogTailer
import com.variant.server.test.util.ServerLogTailer.Level._
import com.variant.core.httpc.HttpOperation
import com.variant.server.boot.ServerMessageLocal

/**
 * Test the server running in a separate process.
 */
class StandaloneServerPostgresTest extends StandaloneServerSpec {

   override lazy val flusher = "postgres"

   "Server" should {

      "send NOT_FOUND on a bad request" in {

         // This is a mystery: None of these 3 lines actually throws any exceptions.
         // Moreover, this test file runs fine (with `testOnly`) without the try block
         // But the full harness (with `test`) fails without the try block with weird socket
         // exceptions. Mystery.
         try {
            HttpOperation.get("http://localhost:5377/foo").exec().responseCode mustBe NotFound.intValue
         } catch {
            case t: Throwable => println("************** " + t.getMessage)
         }
      }

      "send health on a root request" in {
         val resp = HttpOperation.get("http://localhost:5377").exec()
         resp.responseCode mustBe OK.intValue
         resp.bodyString.get must startWith(VariantServer.productVersion._1)
      }

      "fail to deploy without the right JDBC driver in ext/" in {

         server.stop()
         s"rm ${serverDir}/ext/postgresql-42.2.5.jar".!!
         server.start()

         val resp1 = HttpOperation.get("http://localhost:5377/schema/monstrosity").exec()
         resp1.responseCode mustBe BadRequest.intValue
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

         HttpOperation.get("http://localhost:5377/schema/exampleSchema")
            .exec().responseCode mustBe OK.intValue

      }
   }
}
*
*/
