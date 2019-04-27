package com.variant.server.test

import java.io.PrintWriter
import java.util.Random

import com.variant.core.util.StringUtils
import com.variant.core.error.CommonError._
import com.variant.core.error.ServerError._
import com.variant.core.error.UserError.Severity._

import com.variant.server.boot.VariantServer
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

      "send 404 on a bad request" in  {
         
         HttpOperation.get("http://localhost:5377/variant").exec()
            .getResponseCode mustBe 404

         HttpOperation.get("http://localhost:5377/bad").exec()
            .getResponseCode mustBe 404

         HttpOperation.get("http://localhost:5377/variant/bad").exec()
            .getResponseCode mustBe 404
      }
    
      "deploy petclinic and write to the application log" in  {
            
         HttpOperation.get("http://localhost:5377/connection/petclinic")
            .exec().getResponseCode mustBe 200

         val lines = ServerLogTailer.last(2, serverDir + "/log/variant.log")
         lines(0).severity mustBe INFO
         lines(0).message mustBe ServerErrorLocal.SCHEMA_DEPLOYED.asMessage("petclinic", "petclinic.schema")
         lines(1).severity mustBe INFO
         lines(1).message matches ".*\\[432\\].*bootstrapped.*"

      }

      "send health on a root request" in  {
         
         val resp = HttpOperation.get("http://localhost:5377").exec() 
         resp.getResponseCode mustBe 200
         resp.getStringContent must startWith (VariantServer.productName)
      }

      "Start on a non-default port 1234" in  {
         
         server.stop()
         server.start(Map("http.port" -> "1234"))
         
         HttpOperation.get("http://localhost:1234/connection/petclinic")
            .exec().getResponseCode mustBe 200
         
      }

      "Start with alternate config as file" in  {
         
         server.stop()
         
         val fileName = "/tmp/" + StringUtils.random64BitString(rand)
         new PrintWriter(fileName) {
            write("variant.event.flusher.class.name = junk")
            close 
         } 
         
         server.start(Map("variant.config.file"->fileName))
         
         val resp = HttpOperation.get("http://localhost:5377/connection/petclinic").exec()
         resp.getResponseCode mustBe 400
         resp.getErrorContent mustBe UNKNOWN_SCHEMA.asMessage("petclinic")
         val lines = ServerLogTailer.last(4, serverDir + "/log/variant.log")
         lines(0).severity mustBe ERROR
         lines(0).message mustBe ServerErrorLocal.OBJECT_INSTANTIATION_ERROR.asMessage("junk", "java.lang.ClassNotFoundException")
         lines(2).severity mustBe WARN
         lines(2).message mustBe ServerErrorLocal.SCHEMA_FAILED.asMessage("petclinic", s"${serverDir}/schemata/petclinic.schema")

      }

      "Start with alternate config as resource" in  {
         
         server.stop()
         
         val resourceName = StringUtils.random64BitString(rand)
         new PrintWriter(serverDir + "/conf/" + resourceName) {
            write("variant.event.flusher.class.name = junk")
            close 
         } 
         
         server.start(Map("variant.config.resource" -> ("/" + resourceName)))
         
         val resp = HttpOperation.get("http://localhost:5377/connection/petclinic").exec()
         resp.getResponseCode mustBe 400
         resp.getErrorContent mustBe UNKNOWN_SCHEMA.asMessage("petclinic")
         val lines = ServerLogTailer.last(4, serverDir + "/log/variant.log")

         lines(0).severity mustBe ERROR
         lines(0).message mustBe ServerErrorLocal.OBJECT_INSTANTIATION_ERROR.asMessage("junk", "java.lang.ClassNotFoundException")
         lines(2).severity mustBe WARN
         lines(2).message mustBe ServerErrorLocal.SCHEMA_FAILED.asMessage("petclinic", s"${serverDir}/schemata/petclinic.schema")

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