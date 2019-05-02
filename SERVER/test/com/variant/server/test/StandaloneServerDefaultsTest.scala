package com.variant.server.test

import java.io.PrintWriter
import java.util.Random

import play.api.http.Status._
import com.variant.core.util.StringUtils
import com.variant.core.error.ServerError._
import com.variant.core.error.UserError.Severity._

import com.variant.server.boot.VariantServer
import com.variant.server.boot.ServerErrorLocal._
import com.variant.server.test.spec.StandaloneServerSpec
import com.variant.server.test.util.ServerLogTailer
import com.variant.server.util.httpc.HttpOperation
import com.variant.server.boot.ServerErrorLocal

/**
 * Test the server running in a separate process.
 */
class StandaloneServerDefaultsTest extends StandaloneServerSpec {

   val rand = new Random()
   
   "Server" should {

      "send NOT_FOUND on a bad request" in  {
         
         HttpOperation.get("http://localhost:5377/variant").exec()
            .getResponseCode mustBe NOT_FOUND

         HttpOperation.get("http://localhost:5377/bad").exec()
            .getResponseCode mustBe NOT_FOUND

         HttpOperation.get("http://localhost:5377/variant/bad").exec()
            .getResponseCode mustBe NOT_FOUND
      }
    
      "deploy petclinic and write to the application log" in  {
            
         HttpOperation.get("http://localhost:5377/connection/petclinic")
            .exec().getResponseCode mustBe OK

         val lines = ServerLogTailer.last(2, serverDir + "/log/variant.log")
         lines(0).severity mustBe INFO
         lines(0).message mustBe ServerErrorLocal.SCHEMA_DEPLOYED.asMessage("petclinic", "petclinic.schema")
         lines(1).severity mustBe INFO
         lines(1).message matches ".*\\[432\\].*bootstrapped.*"

      }

      "send health on a root request" in  {
         
         val resp = HttpOperation.get("http://localhost:5377").exec() 
         resp.getResponseCode mustBe OK
         resp.getStringContent must startWith (VariantServer.productName)
      }

      "start on a non-default port 1234" in  {
         
         server.stop()
         server.start(Map("http.port" -> "1234"))
         
         HttpOperation.get("http://localhost:1234/connection/petclinic")
            .exec().getResponseCode mustBe OK
         
      }

      "fail to start with alternate config as file because of bad param type." in  {
         
         server.stop()
         
         val fileName = "/tmp/" + StringUtils.random64BitString(rand)
         new PrintWriter(fileName) {
            write("variant.session.timeout = invalid")  // String instead of number
            close 
         } 
         
         var started: Boolean = true;
         
         server.start(
         		commandLineParams = Map("variant.config.file"->fileName),
         		onTimeout = () => started = false)

         started mustBe false
         
         val resp = HttpOperation.get("http://localhost:5377/connection/petclinic").exec()
         resp.getResponseCode mustBe SERVICE_UNAVAILABLE

         val lines = ServerLogTailer.last(4, serverDir + "/log/variant.log")
         lines(0).severity mustBe ERROR
         lines(0).message mustBe ServerErrorLocal.SERVER_BOOT_FAILED.asMessage(VariantServer.productName)
         lines(1).severity mustBe ERROR
         lines(1).message mustBe ServerErrorLocal.CONFIG_PROPERTY_WRONG_TYPE.asMessage("variant.session.timeout", "NUMBER", "STRING")
         lines(2).severity mustBe INFO
         lines(2).message mustBe VariantServer.productName + " is shutting down"
         lines(3).severity mustBe INFO
         lines(3).message must startWith (s"[${ServerErrorLocal.SERVER_SHUTDOWN.getCode}]")

      }

      "Start with alternate config as resource" in  {
         
         server.stop()
         
         val resourceName = StringUtils.random64BitString(rand)
         new PrintWriter(serverDir + "/conf/" + resourceName) {
            write("variant.event.flusher.class.name = junk")
            close 
         } 
         
         // This will cause monster schemas to fail because they don't specity their own flushers.
         server.start(Map("variant.config.resource" -> ("/" + resourceName)))
			val lines = ServerLogTailer.last(12, serverDir + "/log/variant.log")

         lines(0).severity mustBe INFO
			lines(0).message mustBe ServerErrorLocal.SCHEMA_DEPLOYING.asMessage(s"${serverDir}/schemata/monster.schema")
         lines(1).severity mustBe ERROR
         lines(1).message mustBe ServerErrorLocal.OBJECT_INSTANTIATION_ERROR.asMessage("junk", "java.lang.ClassNotFoundException")
         lines(2).severity mustBe ERROR
         lines(2).message mustBe ServerErrorLocal.OBJECT_INSTANTIATION_ERROR.asMessage("junk", "java.lang.ClassNotFoundException")
         lines(3).severity mustBe WARN
         lines(3).message mustBe ServerErrorLocal.SCHEMA_FAILED.asMessage("monstrosity", s"${serverDir}/schemata/monster.schema")
         lines(4).severity mustBe INFO
			lines(4).message mustBe ServerErrorLocal.SCHEMA_DEPLOYING.asMessage(s"${serverDir}/schemata/monster0.schema")
         lines(5).severity mustBe ERROR
         lines(5).message mustBe ServerErrorLocal.OBJECT_INSTANTIATION_ERROR.asMessage("junk", "java.lang.ClassNotFoundException")
         lines(6).severity mustBe ERROR
         lines(6).message mustBe ServerErrorLocal.OBJECT_INSTANTIATION_ERROR.asMessage("junk", "java.lang.ClassNotFoundException")
         lines(7).severity mustBe WARN
         lines(7).message mustBe ServerErrorLocal.SCHEMA_FAILED.asMessage("monstrosity0", s"${serverDir}/schemata/monster0.schema")
         
         val resp = HttpOperation.get("http://localhost:5377/connection/monstrosity").exec()
         resp.getResponseCode mustBe BAD_REQUEST
         resp.getErrorContent mustBe UNKNOWN_SCHEMA.asMessage("monstrosity")

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
