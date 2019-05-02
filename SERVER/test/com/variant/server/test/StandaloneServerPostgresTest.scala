package com.variant.server.test

import java.util.Random
import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.sys.process._
import com.variant.core.error.UserError.Severity._
import com.variant.core.error.ServerError._
import com.variant.server.test.spec.StandaloneServerSpec
import play.api.test.Helpers._
import com.variant.server.util.httpc.HttpOperation
import com.variant.server.test.util.ServerLogTailer
import java.io.PrintWriter
import com.variant.server.boot.ServerErrorLocal
import com.variant.server.boot.VariantServer
import com.variant.server.boot.ServerErrorLocal._
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
            .getResponseCode mustBe NOT_FOUND

         HttpOperation.get("http://localhost:5377/bad").exec()
            .getResponseCode mustBe NOT_FOUND

         HttpOperation.get("http://localhost:5377/variant/bad").exec()
            .getResponseCode mustBe NOT_FOUND

      }
    
      "send health on a root request" in  {
         val resp = HttpOperation.get("http://localhost:5377").exec() 
         resp.getResponseCode mustBe OK
         resp.getStringContent must startWith (VariantServer.productName)
      }

      "fail to deploy petclinic without the right driver in ext/" in  {

         server.stop()
         s"rm ${postgresDriverJar}" ! ;
         server.start()

         val resp1 = HttpOperation.get("http://localhost:5377/connection/monstrosity").exec()
	      resp1.getResponseCode mustBe BAD_REQUEST
   	   resp1.getErrorContent mustBe UNKNOWN_SCHEMA.asMessage("monstrosity")   
         
         val resp2 = HttpOperation.get("http://localhost:5377/connection/monstrosity0").exec()
		   resp2.getResponseCode mustBe BAD_REQUEST
   	   resp2.getErrorContent mustBe UNKNOWN_SCHEMA.asMessage("monstrosity0")
         
         val lines = ServerLogTailer.last(11, serverDir + "/log/variant.log")
         		
         lines(0).severity mustBe ERROR
         lines(0).message mustBe ServerErrorLocal.OBJECT_INSTANTIATION_ERROR.asMessage("com.variant.extapi.std.flush.jdbc.TraceEventFlusherPostgres", "java.lang.reflect.InvocationTargetException")

         // This error goes in the log twice: the first instance has the call stack.
         lines(1).severity mustBe ERROR
         lines(1).message mustBe ServerErrorLocal.OBJECT_INSTANTIATION_ERROR.asMessage("com.variant.extapi.std.flush.jdbc.TraceEventFlusherPostgres", "java.lang.reflect.InvocationTargetException")

         lines(2).severity mustBe WARN
         lines(2).message mustBe ServerErrorLocal.SCHEMA_FAILED.asMessage("monstrosity", s"${serverDir}/schemata/monster.schema")

         lines(4).severity mustBe ERROR
         lines(4).message mustBe ServerErrorLocal.OBJECT_INSTANTIATION_ERROR.asMessage("com.variant.extapi.std.flush.jdbc.TraceEventFlusherPostgres", "java.lang.reflect.InvocationTargetException")

         // This error goes in the log twice: the first instance has the call stack.
         lines(5).severity mustBe ERROR
         lines(5).message mustBe ServerErrorLocal.OBJECT_INSTANTIATION_ERROR.asMessage("com.variant.extapi.std.flush.jdbc.TraceEventFlusherPostgres", "java.lang.reflect.InvocationTargetException")

         lines(6).severity mustBe WARN
         lines(6).message mustBe ServerErrorLocal.SCHEMA_FAILED.asMessage("monstrosity0", s"${serverDir}/schemata/monster0.schema")

         // But petclinic should be ok
	      val resp3 = HttpOperation.get("http://localhost:5377/connection/petclinic").exec()
         resp3.getResponseCode mustBe OK

      }
   }
}
