package com.variant.server.test

import java.io.PrintWriter
import java.util.Random

import com.variant.core.util.StringUtils
import com.variant.core.error.ServerError._
import com.variant.core.util.LogTailer.Level._

import com.variant.server.boot.VariantServer
import com.variant.server.boot.ServerMessageLocal._
import com.variant.server.test.spec.StandaloneServerSpec
import com.variant.server.test.util.ServerLogTailer
import com.variant.server.test.util.ServerLogTailer._
import com.variant.core.httpc.HttpOperation
import akka.http.scaladsl.model.StatusCodes._

/**
 * Test the server running in a separate process.
 */
class StandaloneServerDefaultsTest extends StandaloneServerSpec {

   val rand = new Random()

   "Server" should {

      "send NOT_FOUND on a bad request" in {

         HttpOperation.get("http://localhost:5377/variant").exec().responseCode mustBe NotFound.intValue

         HttpOperation.get("http://localhost:5377/bad").exec().responseCode mustBe NotFound.intValue

         HttpOperation.get("http://localhost:5377/variant/bad").exec().responseCode mustBe NotFound.intValue
      }

      "deploy exampleSchema and write to the application log" in {

         HttpOperation.get("http://localhost:5377/schema/exampleSchema")
            .exec().responseCode mustBe OK.intValue

         val lines = ServerLogTailer.last(2, serverDir + "/log/variant.log")
         lines(0).level mustBe Info
         lines(0).message mustBe SCHEMA_DEPLOYED.asMessage("exampleSchema", "example.schema")
         lines(1).level mustBe INFO
         lines(1).message matches ".*\\[432\\].*bootstrapped.*"

      }

      "send health on a root request" in {

         val resp = HttpOperation.get("http://localhost:5377").exec()
         resp.responseCode mustBe OK.intValue
         resp.bodyString.get must startWith("Variant AIM Server")
      }

      "start on a non-default port 1234" in {

         server.stop()
         server.start(Map("variant.http.port" -> "1234"))

         HttpOperation.get("http://localhost:1234/schema/exampleSchema").exec().responseCode mustBe OK.intValue

      }

      "fail to start with alternate config as file because of bad param type." in {

         server.stop()

         val fileName = "/tmp/" + StringUtils.random64BitString(rand)
         new PrintWriter(fileName) {
            write("variant.session.timeout = invalid") // String instead of number
            close
         }

         var started: Boolean = true;

         server.start(
            commandLineParams = Map("variant.config.file" -> fileName),
            onTimeout = () => started = false)

         started mustBe false

         val lines = ServerLogTailer.last(1, serverDir + "/log/variant.log")
         lines(0).level mustBe ERROR
         lines(0).message mustBe CONFIG_PROPERTY_WRONG_TYPE.asMessage("variant.session.timeout", "NUMBER", "STRING")

         // Misconfig causes server staartup to abort, so there's no server.
         intercept[java.net.ConnectException] {
            HttpOperation.get("http://localhost:5377").exec()
         }

      }

      "Start with alternate config as resource" in {

         server.stop()

         val resourceName = StringUtils.random64BitString(rand)
         new PrintWriter(serverDir + "/conf/" + resourceName) {
            write("variant.event.flusher.class.name = junk")
            close
         }

         // This will cause monster schemas to fail because they don't specity their own flushers.
         server.start(Map("variant.config.resource" -> ("/" + resourceName)))
         val lines = ServerLogTailer.last(4, serverDir + "/log/variant.log")

         lines(0).level mustBe INFO
         lines(0).message mustBe SCHEMA_DEPLOYING.asMessage(s"${serverDir}/schemata/example.schema")
         lines(1).level mustBe ERROR
         lines(1).message mustBe OBJECT_INSTANTIATION_ERROR.asMessage("junk", "java.lang.ClassNotFoundException")
         lines(2).level mustBe WARN
         lines(2).message mustBe SCHEMA_FAILED.asMessage("exampleSchema", s"${serverDir}/schemata/example.schema")
         lines(3).level mustBe INFO
         lines(3).message matches ".*\\[432\\].*bootstrapped.*"

         val resp = HttpOperation.get("http://localhost:5377/schema/monstrosity").exec()
         resp.responseCode mustBe BadRequest.intValue
         resp.getErrorContent mustBe UNKNOWN_SCHEMA.asMessage("monstrosity")

      }

      "Fail to start with bad alternate config as file" in {

         server.stop()

         server.start(
            Map("variant.config.file" -> "non-existent"),
            5,
            () => {
               val lines = server.err.toArray[String](new Array[String](10))
               lines.foreach(l => println("************ " + l))
               lines(0) must include("cannot start the server")
               lines(1) must include(CONFIG_FILE_NOT_FOUND.asMessage("non-existent"))
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
   }
}
