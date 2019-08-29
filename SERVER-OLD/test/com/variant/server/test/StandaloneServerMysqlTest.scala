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
class StandaloneServerMysqlTest extends StandaloneServerSpec {

   val rand = new Random()
   
   override lazy val flusher = "mysql"

   "Server" should {

      "send NOT_FOUND on a bad request" in  {
         
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

      "fail to deploy petclinic without the right JDBC driver in ext/" in  {

         server.stop()
         s"rm ${serverDir}/ext/mysql-connector-java-8.0.11.jar" ! ;
         server.start()
         
         val resp1 = HttpOperation.get("http://localhost:5377/connection/monstrosity").exec()
	      resp1.getResponseCode mustBe BAD_REQUEST
   	   resp1.getErrorContent mustBe UNKNOWN_SCHEMA.asMessage("monstrosity")   
         
         val resp2 = HttpOperation.get("http://localhost:5377/connection/monstrosity0").exec()
		   resp2.getResponseCode mustBe BAD_REQUEST
   	   resp2.getErrorContent mustBe UNKNOWN_SCHEMA.asMessage("monstrosity0")
         
         val lines = ServerLogTailer.last(11, serverDir + "/log/variant.log")

         lines(0).severity mustBe ERROR
         lines(0).message mustBe ServerErrorLocal.OBJECT_INSTANTIATION_ERROR.asMessage("com.variant.extapi.std.flush.jdbc.TraceEventFlusherMysql", "java.lang.reflect.InvocationTargetException")

         // This error goes in the log twice: the first instance has the call stack.
         lines(1).severity mustBe ERROR
         lines(1).message mustBe ServerErrorLocal.OBJECT_INSTANTIATION_ERROR.asMessage("com.variant.extapi.std.flush.jdbc.TraceEventFlusherMysql", "java.lang.reflect.InvocationTargetException")

         lines(2).severity mustBe WARN
         lines(2).message mustBe ServerErrorLocal.SCHEMA_FAILED.asMessage("monstrosity", s"${serverDir}/schemata/monster.schema")

         lines(4).severity mustBe ERROR
         lines(4).message mustBe ServerErrorLocal.OBJECT_INSTANTIATION_ERROR.asMessage("com.variant.extapi.std.flush.jdbc.TraceEventFlusherMysql", "java.lang.reflect.InvocationTargetException")

         // This error goes in the log twice: the first instance has the call stack.
         lines(5).severity mustBe ERROR
         lines(5).message mustBe ServerErrorLocal.OBJECT_INSTANTIATION_ERROR.asMessage("com.variant.extapi.std.flush.jdbc.TraceEventFlusherMysql", "java.lang.reflect.InvocationTargetException")

         lines(6).severity mustBe WARN
         lines(6).message mustBe ServerErrorLocal.SCHEMA_FAILED.asMessage("monstrosity0", s"${serverDir}/schemata/monster0.schema")

         // But petclinic should be ok
	      val resp3 = HttpOperation.get("http://localhost:5377/connection/petclinic").exec()
         resp3.getResponseCode mustBe OK

      }

      "Redeploy moster schemas after restoring the JDBC driver in ext/" in  {
         
         // Hot redeploy won't work. Not sure why, prob. class loader will refuse to look for the same class twice.

         server.stop()
         
         s"cp standalone-server/ext/mysql-connector-java-8.0.11.jar ${serverDir}/ext/" ! ;

         server.start()
         
         HttpOperation.get("http://localhost:5377/connection/petclinic")
            .exec().getResponseCode mustBe OK
 
         HttpOperation.get("http://localhost:5377/connection/monstrosity")
            .exec().getResponseCode mustBe OK
 
         HttpOperation.get("http://localhost:5377/connection/monstrosity0")
            .exec().getResponseCode mustBe OK
 
      }

      "Start on a non-default port 1234" in  {
         
         server.stop()
         server.start(Map("http.port" -> "1234"))
         
         HttpOperation.get("http://localhost:1234/connection/petclinic")
            .exec().getResponseCode mustBe OK
         
      }

      "Start with alternate config as file" in  {
         
         server.stop()
         
         val fileName = "/tmp/" + StringUtils.random64BitString(rand)
         new PrintWriter(fileName) {
            write("variant.event.flusher.class.name = junk")
            close 
         } 
         
         server.start(Map("variant.config.file"->fileName))
         
         val resp1 = HttpOperation.get("http://localhost:5377/connection/monstrosity").exec()
         resp1.getResponseCode mustBe BAD_REQUEST
         resp1.getErrorContent mustBe UNKNOWN_SCHEMA.asMessage("monstrosity")
         
         val resp2 = HttpOperation.get("http://localhost:5377/connection/monstrosity0").exec()
         resp2.getResponseCode mustBe BAD_REQUEST
         resp2.getErrorContent mustBe UNKNOWN_SCHEMA.asMessage("monstrosity0")

         val lines = ServerLogTailer.last(11, serverDir + "/log/variant.log")

         lines(0).severity mustBe ERROR
         lines(0).message mustBe ServerErrorLocal.OBJECT_INSTANTIATION_ERROR.asMessage("junk", "java.lang.ClassNotFoundException")

         // This error goes in the log twice: the first instance has the call stack.
         lines(1).severity mustBe ERROR
         lines(1).message mustBe ServerErrorLocal.OBJECT_INSTANTIATION_ERROR.asMessage("junk", "java.lang.ClassNotFoundException")

         lines(2).severity mustBe WARN
         lines(2).message mustBe ServerErrorLocal.SCHEMA_FAILED.asMessage("monstrosity", s"${serverDir}/schemata/monster.schema")

         lines(4).severity mustBe ERROR
         lines(4).message mustBe ServerErrorLocal.OBJECT_INSTANTIATION_ERROR.asMessage("junk", "java.lang.ClassNotFoundException")

         // This error goes in the log twice: the first instance has the call stack.
         lines(5).severity mustBe ERROR
         lines(5).message mustBe ServerErrorLocal.OBJECT_INSTANTIATION_ERROR.asMessage("junk", "java.lang.ClassNotFoundException")

         lines(6).severity mustBe WARN
         lines(6).message mustBe ServerErrorLocal.SCHEMA_FAILED.asMessage("monstrosity0", s"${serverDir}/schemata/monster0.schema")

         // But petclinic should be ok
	      val resp3 = HttpOperation.get("http://localhost:5377/connection/petclinic").exec()
         resp3.getResponseCode mustBe OK

      }

      "Start with alternate config as resource" in  {
         
         server.stop()
         
         val resourceName = StringUtils.random64BitString(rand)
         new PrintWriter(serverDir + "/conf/" + resourceName) {
            write("variant.event.flusher.class.name = junk")
            close 
         } 
         
         server.start(Map("variant.config.resource" -> ("/" + resourceName)))
         
         val resp1 = HttpOperation.get("http://localhost:5377/connection/monstrosity").exec()
         resp1.getResponseCode mustBe BAD_REQUEST
         resp1.getErrorContent mustBe UNKNOWN_SCHEMA.asMessage("monstrosity")
         
         val resp2 = HttpOperation.get("http://localhost:5377/connection/monstrosity0").exec()
         resp2.getResponseCode mustBe BAD_REQUEST
         resp2.getErrorContent mustBe UNKNOWN_SCHEMA.asMessage("monstrosity0")

         val lines = ServerLogTailer.last(11, serverDir + "/log/variant.log")

         lines(0).severity mustBe ERROR
         lines(0).message mustBe ServerErrorLocal.OBJECT_INSTANTIATION_ERROR.asMessage("junk", "java.lang.ClassNotFoundException")

         // This error goes in the log twice: the first instance has the call stack.
         lines(1).severity mustBe ERROR
         lines(1).message mustBe ServerErrorLocal.OBJECT_INSTANTIATION_ERROR.asMessage("junk", "java.lang.ClassNotFoundException")

         lines(2).severity mustBe WARN
         lines(2).message mustBe ServerErrorLocal.SCHEMA_FAILED.asMessage("monstrosity", s"${serverDir}/schemata/monster.schema")

         lines(4).severity mustBe ERROR
         lines(4).message mustBe ServerErrorLocal.OBJECT_INSTANTIATION_ERROR.asMessage("junk", "java.lang.ClassNotFoundException")

         // This error goes in the log twice: the first instance has the call stack.
         lines(5).severity mustBe ERROR
         lines(5).message mustBe ServerErrorLocal.OBJECT_INSTANTIATION_ERROR.asMessage("junk", "java.lang.ClassNotFoundException")

         lines(6).severity mustBe WARN
         lines(6).message mustBe ServerErrorLocal.SCHEMA_FAILED.asMessage("monstrosity0", s"${serverDir}/schemata/monster0.schema")

         // But petclinic should be ok
	      val resp3 = HttpOperation.get("http://localhost:5377/connection/petclinic").exec()
         resp3.getResponseCode mustBe OK

      }

      "Fail to start with bad alternate config as file" in  {
         
         server.stop()
                  
         server.start(
               Map("variant.config.file"->"non-existent"),
               5,
               () => {
                  val errLines = server.err.toArray[String](new Array[String](10))
                  errLines(0) must include ("cannot start the server")
                  errLines(1) must include (CONFIG_FILE_NOT_FOUND.asMessage("non-existent"))
               }
         )         
      }

      "Fail to start with bad alternate config as resource" in  {
         
         server.stop()
                  
         server.start(
               Map("variant.config.resource"->"non-existent"),
               5,
               () => {
                  val errLines = server.err.toArray[String](new Array[String](10))
                  errLines(0) must include ("cannot start the server")
                  errLines(1) must include (CONFIG_RESOURCE_NOT_FOUND.asMessage("non-existent"))
               }
         )         
      }

      "Fail to start when conflicting alternate configs" in  {
         
         server.stop()
                  
         server.start(
               Map("variant.config.file"->"foo","variant.config.resource"->"bar"),
               5,
               () => {
                  val errLines = server.err.toArray[String](new Array[String](10))
                  errLines(0) must include ("cannot start the server")
                  errLines(1) must include (CONFIG_BOTH_FILE_AND_RESOURCE_GIVEN.asMessage())
               }
         )         
      }
   }
}
