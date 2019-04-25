package com.variant.server.test

import java.util.Random
import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.sys.process._
import com.variant.core.error.UserError.Severity._
import com.variant.core.error.CommonError._
import com.variant.core.error.ServerError._
import com.variant.server.test.spec.StandaloneServerSpec
import play.api.test.Helpers._
import com.variant.server.util.httpc.HttpOperation
import com.variant.server.test.util.ServerLogTailer
import java.io.PrintWriter
import com.variant.server.boot.ServerErrorLocal
import com.variant.server.boot.VariantServer
import com.variant.core.util.StringUtils
/**
 * Test the server running in a separate process.
 */
class StandaloneServerPostgresTest extends StandaloneServerSpec {

   override lazy val flusher = "postgres"

   private val rand = new Random()
   private val postgresDriverJar = s"${serverDir}/ext/postgresql-42.2.5.jar"

   "Server" should {

      "send 404 on a bad request" in  {
         
         HttpOperation.get("http://localhost:5377/variant").exec()
            .getResponseCode mustBe 404

         HttpOperation.get("http://localhost:5377/bad").exec()
            .getResponseCode mustBe 404

         HttpOperation.get("http://localhost:5377/variant/bad").exec()
            .getResponseCode mustBe 404

      }
    
      "send health on a root request" in  {
         val resp = HttpOperation.get("http://localhost:5377").exec() 
         resp.getResponseCode mustBe 200
         resp.getStringContent must startWith (VariantServer.productName)
      }

      "fail to deploy petclinic without the right driver in ext/" in  {

         server.stop()
         s"rm ${postgresDriverJar}" ! ;
         server.start()
         val resp = HttpOperation.get("http://localhost:5377/connection/petclinic").exec()
         resp.getResponseCode mustBe 400
         resp.getErrorContent mustBe UNKNOWN_SCHEMA.asMessage("petclinic")
         
         val lines = ServerLogTailer.last(4, serverDir + "/log/variant.log")

         lines(0).severity mustBe ERROR
         lines(0).message mustBe ServerErrorLocal.OBJECT_INSTANTIATION_ERROR.asMessage("com.variant.extapi.std.flush.jdbc.TraceEventFlusherPostgres", "java.lang.reflect.InvocationTargetException")

         // This error goes in the log twice: the first instance has the call stack.
         lines(1).severity mustBe ERROR
         lines(1).message mustBe ServerErrorLocal.OBJECT_INSTANTIATION_ERROR.asMessage("com.variant.extapi.std.flush.jdbc.TraceEventFlusherPostgres", "java.lang.reflect.InvocationTargetException")

         lines(2).severity mustBe WARN
         lines(2).message mustBe ServerErrorLocal.SCHEMA_FAILED.asMessage("petclinic", s"${serverDir}/schemata/petclinic.schema")

      }
   }
}
