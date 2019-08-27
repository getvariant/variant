package com.variant.server.test

import java.io.PrintWriter

import com.variant.core.util.StringUtils
import com.variant.core.error.ServerError._

import com.variant.server.boot.VariantServer
import com.variant.server.boot.ServerMessageLocal._
import com.variant.server.test.spec.StandaloneServerSpec
import com.variant.server.test.util.ServerLogTailer
import com.variant.server.test.util.ServerLogTailer.Level._
import com.variant.core.httpc.HttpOperation
import akka.http.scaladsl.model.StatusCodes._

/**
 * Test the server running in a separate process.
 */
class StandaloneServerDefaultTest extends StandaloneServerSpec {

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

      "deploy exampleSchema and write to the application log" in {

         HttpOperation.get("http://localhost:5377/schema/exampleSchema")
            .exec().responseCode mustBe OK.intValue

         val lines = ServerLogTailer.last(2, serverDir + "/log/variant.log")
         lines(0).level mustBe Info
         lines(0).message mustBe SCHEMA_DEPLOYED.asMessage("exampleSchema", "example.schema")
         lines(1).level mustBe Info
         lines(1).message must include regex s"\\[${SERVER_BOOT_OK.getCode}\\].*started on port"

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

         val fileName = "/tmp/" + StringUtils.random64BitString(new java.util.Random)
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
         lines(0).level mustBe Error
         lines(0).message mustBe CONFIG_PROPERTY_WRONG_TYPE.asMessage("variant.session.timeout", "NUMBER", "STRING")

         // Misconfig causes server staartup to abort, so there's no server.
         intercept[java.net.ConnectException] {
            HttpOperation.get("http://localhost:5377").exec()
         }

      }

      "Start with alternate config as resource" in {

         server.stop()

         val resourceName = StringUtils.random64BitString(new java.util.Random)
         new PrintWriter(serverDir + "/conf/" + resourceName) {
            write("variant.event.flusher.class.name = junk")
            close
         }

         // This will cause monster schemas to fail because they don't specity their own flushers.
         server.start(Map("variant.config.resource" -> ("/" + resourceName)))
         val lines = ServerLogTailer.last(4, serverDir + "/log/variant.log")

         lines(0).level mustBe Info
         lines(0).message mustBe SCHEMA_DEPLOYING.asMessage(s"${serverDir}/schemata/example.schema")
         lines(1).level mustBe Error
         lines(1).message mustBe OBJECT_INSTANTIATION_ERROR.asMessage("junk", "java.lang.ClassNotFoundException")
         lines(2).level mustBe Warn
         lines(2).message mustBe SCHEMA_FAILED.asMessage("exampleSchema", s"${serverDir}/schemata/example.schema")
         lines(3).level mustBe Info
         lines(3).message must include regex s"\\[${SERVER_BOOT_OK.getCode}\\].*started on port"

         val resp = HttpOperation.get("http://localhost:5377/schema/monstrosity").exec()
         resp.responseCode mustBe BadRequest.intValue
         resp.getErrorContent mustBe UNKNOWN_SCHEMA.asMessage("monstrosity")

      }

      "Fail to start with bad alternate config as file" in {

         server.stop()

         server.start(
            commandLineParams = Map("variant.config.file" -> "non-existent"),
            onTimeout = () => ( /*don't throw timeout exception*/ ))

         val lines = ServerLogTailer.last(1, serverDir + "/log/variant.log")
         lines(0).level mustBe Error
         lines(0).message mustBe CONFIG_FILE_NOT_FOUND.asMessage("non-existent")

      }

      "Fail to start with bad alternate config as resource" in {

         server.stop()

         server.start(
            commandLineParams = Map("variant.config.resource" -> "non-existent"),
            onTimeout = () => ( /*don't throw timeout exception*/ ))

         val lines = ServerLogTailer.last(1, serverDir + "/log/variant.log")
         lines(0).level mustBe Error
         lines(0).message mustBe CONFIG_RESOURCE_NOT_FOUND.asMessage("non-existent")

      }

      "Fail to start when conflicting alternate configs" in {

         server.stop()

         server.start(
            commandLineParams = Map("variant.config.file" -> "foo", "variant.config.resource" -> "bar"),
            onTimeout = () => ( /*don't throw timeout exception*/ ))

         val lines = ServerLogTailer.last(1, serverDir + "/log/variant.log")
         lines(0).level mustBe Error
         lines(0).message mustBe CONFIG_BOTH_FILE_AND_RESOURCE_GIVEN.asMessage()
      }
   }
}
