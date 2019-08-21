package com.variant.server.test

import scala.sys.process._

import com.variant.server.boot.ServerExceptionLocal
import com.variant.server.boot.ServerMessageLocal._
import com.variant.server.boot.VariantServer
import com.variant.server.impl.ConfigKeys
import com.variant.server.test.routes.SessionTest
import com.variant.server.test.spec.EmbeddedServerSpec
import com.variant.server.test.util.ServerLogTailer
import com.variant.server.test.util.ServerLogTailer.Level._

import akka.http.scaladsl.model.HttpMethods
import akka.http.scaladsl.model.HttpMethods.GET
import akka.http.scaladsl.model.HttpMethods.POST
import akka.http.scaladsl.model.HttpMethods.PUT
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.StatusCodes.NotFound
import akka.http.scaladsl.model.StatusCodes.ServiceUnavailable

/**
 * Test various schema deployment error scenarios
 */
class ServerBootExceptionTest extends EmbeddedServerSpec with ConfigKeys {

   "Server boot" should {

      "emit CONFIG_PROPERTY_NOT_SET if no schema.dir config" in {

         reboot(
            VariantServer.builder
               .withoutConfiguration(Seq("variant.schemata.dir")))

         server.isUp mustBe false
         server.schemata.size mustBe 0
         server.bootExceptions.size mustEqual 1
         val ex = server.bootExceptions.head
         ex mustEqual ServerExceptionLocal(CONFIG_PROPERTY_NOT_SET, SCHEMATA_DIR)
         server.isUp mustBe false
      }
   }

   "emit SCHEMATA_DIR_MISSING if schemata dir does not exist" in {

      reboot(
         VariantServer.builder
            .withConfiguration(Map("variant.schemata.dir" -> "non-existent")))

      server.isUp mustBe false
      server.schemata.size mustBe 0
      server.bootExceptions.size mustEqual 1
      val ex = server.bootExceptions.head
      ex mustEqual ServerExceptionLocal(SCHEMATA_DIR_MISSING, "non-existent")
   }

   "return NotFound in every unmapped http request after SCHEMATA_DIR_MISSING" in {

      server.isUp mustBe false

      HttpRequest(method = GET, uri = "/bad/path") ~> router ~> check {
         handled mustBe true
         status mustBe NotFound
      }

   }

   "return ServiceUnavailable in every mapped http request after SCHEMATA_DIR_MISSING" in {

      server.isUp mustBe false

      HttpRequest(method = GET, uri = "/schema/petclinic") ~> router ~> check {
         handled mustBe true
         status mustBe ServiceUnavailable
      }

      HttpRequest(method = POST, uri = "/session/petclinic/foo") ~> router ~> check {
         handled mustBe true
         status mustBe ServiceUnavailable
      }

      HttpRequest(method = PUT, uri = "/session-attr/monstrosity/foo") ~> router ~> check {
         handled mustBe true
         status mustBe ServiceUnavailable
      }
   }

   "Schemata dir which is not a dir" should {

      "cause server to throw SCHEMATA_DIR_NOT_DIR" in {

         reboot(
            VariantServer.builder
               .withConfiguration(Map("variant.schemata.dir" -> "schemata-file")))

         server.isUp mustBe false
         server.schemata.size mustBe 0
         server.bootExceptions.size mustEqual 1
         val ex = server.bootExceptions.head
         ex mustEqual ServerExceptionLocal(SCHEMATA_DIR_NOT_DIR, "schemata-file")
      }
   }

   "Multiple schemata with duplicate schema name" should {

      "cause server to throw SCHEMA_NAME_DUPE" in {

         val schemataDir = "/tmp/schemata-test"
         s"rm -rf ${schemataDir}" !;
         s"mkdir ${schemataDir}" !;
         s"cp schemata/monster.schema ${schemataDir}/monster1.schema" !;
         s"cp schemata/monster.schema ${schemataDir}/monster2.schema" !;

         reboot(
            VariantServer.builder
               .withConfiguration(Map("variant.schemata.dir" -> schemataDir)))

         val logTail = ServerLogTailer.last(3)
         server.schemata.size mustBe 1
         val errLine = logTail(0)
         errLine.level mustBe Error
         errLine.message must include(SCHEMA_CANNOT_REPLACE.asMessage("monstrosity", "monster1.schema", "monster2.schema"))
         server.isUp mustBe true
      }

      "ensure monster schema is accessible" in {

         HttpRequest(method = HttpMethods.POST, uri = "/session/monstrosity/foo", entity = SessionTest.emptyTargetingTrackerBody) ~> router ~> check {
            val ssnResp = SessionResponse(response)
            ssnResp.schema.getMeta.getName mustBe "monstrosity"
         }
      }

   }
}
