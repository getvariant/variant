package com.variant.server.test

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
class StandaloneServerMysqlTest extends StandaloneServerSpec {

   override lazy val flusher = "mysql"

   "Server" should {

      "send NOT_FOUND on a bad request" in {

         HttpOperation.get("http://localhost:5377/variant").exec().responseCode mustBe NotFound.intValue

         HttpOperation.get("http://localhost:5377/bad").exec().responseCode mustBe NotFound.intValue

         HttpOperation.get("http://localhost:5377/variant/bad").exec().responseCode mustBe NotFound.intValue
      }

      "send health on a root request" in {
         val resp = HttpOperation.get("http://localhost:5377").exec()
         resp.responseCode mustBe OK.intValue
         resp.bodyString.get must startWith(VariantServer.productVersion._1)
      }

      "fail to deploy without the right JDBC driver in ext/" in {

         server.stop()
         s"rm ${serverDir}/ext/mysql-connector-java-8.0.11.jar".!!
         server.start()

         val resp1 = HttpOperation.get("http://localhost:5377/schema/monstrosity").exec()
         resp1.responseCode mustBe BadRequest.intValue
         resp1.getErrorContent mustBe UNKNOWN_SCHEMA.asMessage("monstrosity")

         val lines = ServerLogTailer.last(3, serverDir + "/log/variant.log")
         lines(0).level mustBe Error
         lines(0).message mustBe OBJECT_INSTANTIATION_ERROR.asMessage("com.variant.extapi.std.flush.jdbc.TraceEventFlusherMysql", "java.lang.reflect.InvocationTargetException")
         lines(1).level mustBe Warn
         lines(1).message mustBe SCHEMA_FAILED.asMessage("exampleSchema", s"${serverDir}/schemata/example.schema")
         lines(2).level mustBe Info
         lines(2).message must include regex s"\\[${ServerMessageLocal.SERVER_BOOT_OK.getCode}\\].*started on port"

      }

      "Redeploy example schema after restoring the JDBC driver in ext/" in {

         // Hot redeploy won't work. Not sure why, prob. class loader will refuse to look for the same class twice.
         server.stop()

         // Our current directory at test runtime is /test-base
         s"cp ../standalone-server/ext/mysql-connector-java-8.0.11.jar ${serverDir}/ext/" !!;

         server.start()

         HttpOperation.get("http://localhost:5377/schema/exampleSchema")
            .exec().responseCode mustBe OK.intValue

      }

      "Start on a non-default port 1234" in {

         server.stop()
         server.start(Map("variant.http.port" -> "1234"))

         HttpOperation.get("http://localhost:1234/schema/exampleSchema")
            .exec().responseCode mustBe OK.intValue

      }

      "Start with alternate config as file" in {

         server.stop()

         val fileName = "/tmp/" + StringUtils.random64BitString(new java.util.Random)
         new PrintWriter(fileName) {
            write("variant.event.flusher.class.name = junk")
            close
         }

         server.start(Map("variant.config.file" -> fileName))

         val resp1 = HttpOperation.get("http://localhost:5377/schema/exampleSchema").exec()
         resp1.responseCode mustBe BadRequest.intValue
         resp1.getErrorContent mustBe UNKNOWN_SCHEMA.asMessage("exampleSchema")

         val lines = ServerLogTailer.last(3, serverDir + "/log/variant.log")
         lines(0).level mustBe Error
         lines(0).message mustBe OBJECT_INSTANTIATION_ERROR.asMessage("junk", "java.lang.ClassNotFoundException")
         lines(1).level mustBe Warn
         lines(1).message mustBe SCHEMA_FAILED.asMessage("exampleSchema", s"${serverDir}/schemata/example.schema")
         lines(2).level mustBe Info
         lines(2).message must include regex s"\\[${ServerMessageLocal.SERVER_BOOT_OK.getCode}\\].*started on port"

      }
      /*
      "Start with alternate config as resource" in {

         server.stop()

         val resourceName = StringUtils.random64BitString(new java.util.Random)
         new PrintWriter(serverDir + "/conf/" + resourceName) {
            write("variant.event.flusher.class.name = junk")
            close
         }

         server.start(Map("variant.config.resource" -> ("/" + resourceName)))

         val resp1 = HttpOperation.get("http://localhost:5377/connection/monstrosity").exec()
         resp1.responseCode mustBe BadRequest.intValue
         resp1.getErrorContent mustBe UNKNOWN_SCHEMA.asMessage("monstrosity")

         val resp2 = HttpOperation.get("http://localhost:5377/connection/monstrosity0").exec()
         resp2.responseCode mustBe BadRequest.intValue
         resp2.getErrorContent mustBe UNKNOWN_SCHEMA.asMessage("monstrosity0")

         val lines = ServerLogTailer.last(11, serverDir + "/log/variant.log")

         lines(0).level mustBe Error
         lines(0).message mustBe OBJECT_INSTANTIATION_ERROR.asMessage("junk", "java.lang.ClassNotFoundException")

         // This error goes in the log twice: the first instance has the call stack.
         lines(1).level mustBe Error
         lines(1).message mustBe OBJECT_INSTANTIATION_ERROR.asMessage("junk", "java.lang.ClassNotFoundException")

         lines(2).level mustBe Warn
         lines(2).message mustBe SCHEMA_FAILED.asMessage("monstrosity", s"${serverDir}/schemata/monster.schema")

         lines(4).level mustBe Error
         lines(4).message mustBe OBJECT_INSTANTIATION_ERROR.asMessage("junk", "java.lang.ClassNotFoundException")

         // This error goes in the log twice: the first instance has the call stack.
         lines(5).level mustBe Error
         lines(5).message mustBe OBJECT_INSTANTIATION_ERROR.asMessage("junk", "java.lang.ClassNotFoundException")

         lines(6).level mustBe Warn
         lines(6).message mustBe SCHEMA_FAILED.asMessage("monstrosity0", s"${serverDir}/schemata/monster0.schema")

         // But petclinic should be ok
         val resp3 = HttpOperation.get("http://localhost:5377/connection/petclinic").exec()
         resp3.responseCode mustBe OK

      }

      "Fail to start with bad alternate config as file" in {

         server.stop()

         server.start(
            Map("variant.config.file" -> "non-existent"),
            5,
            () => {
               val errLines = server.err.toArray[String](new Array[String](10))
               errLines(0) must include("cannot start the server")
               errLines(1) must include(CONFIG_FILE_NOT_FOUND.asMessage("non-existent"))
            })
      }

      "Fail to start with bad alternate config as resource" in {

         server.stop()

         server.start(
            Map("variant.config.resource" -> "non-existent"),
            5,
            () => {
               val errLines = server.err.toArray[String](new Array[String](10))
               errLines(0) must include("cannot start the server")
               errLines(1) must include(CONFIG_RESOURCE_NOT_FOUND.asMessage("non-existent"))
            })
      }

      "Fail to start when conflicting alternate configs" in {

         server.stop()

         server.start(
            Map("variant.config.file" -> "foo", "variant.config.resource" -> "bar"),
            5,
            () => {
               val errLines = server.err.toArray[String](new Array[String](10))
               errLines(0) must include("cannot start the server")
               errLines(1) must include(CONFIG_BOTH_FILE_AND_RESOURCE_GIVEN.asMessage())
            })
      }
      *
      */
   }
}
