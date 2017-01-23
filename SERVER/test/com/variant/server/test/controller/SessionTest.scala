package com.variant.server.test.controller

import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._
import scala.collection.JavaConversions._
import com.variant.server.test.util.ParamString
import com.variant.server.ConfigKeys
import com.variant.server.test.BaseSpecWithServer
import com.variant.core.exception.ServerError._
import com.variant.core.util.VariantStringUtils
import play.api.libs.json._


/**
 * Session Controller Tests
 */
object SessionTest {
   val sessionJsonProto = """
      {"sid":"${sid:SID}",
       "ts": ${ts:%d}, 
       "schid": "%s",
       "request": {"state": "state1","status": "OK","committed": true, 
                  "params": [{"name": "PARAM ONE", "value": "Param One Value"},{"name": "PARAM TWO", "value": "Param Two Value"}], 
                  "exps": ["test1.A.true","test2.B.false","test3.C.false"]},
        "states": [{"state": "state1","count": 23}, {"state": "state2","count": 32}],
        "tests": ["test1","test2"]
      }
   """
}

class SessionTest extends BaseSpecWithServer {
   
   import SessionTest._
   
   val endpoint = context + "/session"
   val schemaId = server.schema.get.getId
   val sessionJson = ParamString(sessionJsonProto.format(System.currentTimeMillis(), schemaId))

   "SessionController" should {

      "return 404 on GET no SID" in {
         
         val resp = route(app, FakeRequest(GET, endpoint + "/").withHeaders("Content-Type" -> "text/plain")).get
         status(resp) mustBe NOT_FOUND
         contentAsString(resp) mustBe empty
      }

      var connId: String = null
      
      "obtain a connection" in {
         // POST new connection
         val connResp = route(app, FakeRequest(POST, context + "/connection/big_covar_schema")).get
         status(connResp) mustBe OK
         val json = contentAsJson(connResp) 
         json mustNot be (null)
         connId = (json \ "id").as[String]
         connId mustNot be (null)
      }

      "return SessionExpired on GET non-existent session" in {  
         
         val resp = route(app, FakeRequest(GET, endpoint + "/" + scid("foo",connId))).get
         status(resp) mustBe BAD_REQUEST
         val (isInternal, error, args) = parseError(contentAsJson(resp))
         isInternal mustBe false 
         error mustBe SessionExpired
         args mustBe empty
      }

      "return OK on PUT non-existent session" in {
         
         val body = Json.obj(
            "cid" -> connId,
            "ssn" -> sessionJson.expand("sid" -> "foo")
            ).toString
         val resp = route(app, FakeRequest(PUT, endpoint).withTextBody(body)).get
         status(resp) mustBe OK
         contentAsString(resp) mustBe empty
      }

      "return OK and existing session on GET" in {
       
         val resp = route(app, FakeRequest(GET, endpoint + "/" + scid("foo",connId))).get
         status(resp) mustBe OK
         val respAsJson = contentAsJson(resp)
         VariantStringUtils.digest((respAsJson \ "session").as[String]) mustBe 
            VariantStringUtils.digest(sessionJson.expand("sid" -> "foo").toString())
      }

      "return OK and replace existing session on PUT" in {
       
         val body = Json.obj(
            "cid" -> connId,
            "ssn" -> sessionJson.expand("sid" -> "foo2")
            ).toString

         val reqPut = FakeRequest(PUT, endpoint).withTextBody(body)
         val respPut = route(app, reqPut).get
         status(respPut) mustBe OK
         contentAsString(respPut) mustBe empty
         
         val respGet = route(app, FakeRequest(GET, endpoint + "/" + scid("foo",connId))).get
         status(respGet) mustBe OK
         val respAsJson = contentAsJson(respGet)
         VariantStringUtils.digest((respAsJson \ "session").as[String]) mustBe 
            VariantStringUtils.digest(sessionJson.expand("sid" -> "foo"))
      }

      "return OK and create session on PUT" in {
       
         val body = Json.obj(
            "cid" -> connId,
            "ssn" -> sessionJson.expand("sid" -> "bar1")
            ).toString
         val reqPut = FakeRequest(PUT, endpoint).withTextBody(body)
         val respPut = route(app, reqPut).get
         status(respPut) mustBe OK
         contentAsString(respPut) mustBe empty
         
         val respGet = route(app, FakeRequest(GET, endpoint + "/" + scid("bar1", connId))).get
         status(respGet) mustBe OK
         val respAsJson = contentAsJson(respGet)
         VariantStringUtils.digest((respAsJson \ "session").as[String]) mustBe 
            VariantStringUtils.digest(sessionJson.expand("sid" -> "bar1"))
      }

     "not lose existing session with different key" in {

         val resp = route(app, FakeRequest(GET, endpoint + "/" + scid("foo",connId))).get
         status(resp) mustBe OK
         val respAsJson = contentAsJson(resp)
         VariantStringUtils.digest((respAsJson \ "session").as[String]) mustBe 
            VariantStringUtils.digest(sessionJson.expand("sid" -> "foo"))
      }

      "return SessionExpired on GET expired session" in {
         
         val timeout = server.config.getLong(ConfigKeys.SESSION_TIMEOUT)
         val vacuumInterval = server.config.getLong(ConfigKeys.SESSION_STORE_VACUUM_INTERVAL)
         timeout  mustEqual 1
         vacuumInterval  mustEqual 1
      
         Thread.sleep((timeout * 1000 * 2).asInstanceOf[Long]);
      
         ("foo" :: "bar" :: Nil).foreach { sid =>
              val resp = route(app, FakeRequest(GET, endpoint + "/" + scid(sid,connId))).get
              val (isInternal, error, args) = parseError(contentAsJson(resp))
              isInternal mustBe SessionExpired.isInternal() 
              error mustBe SessionExpired
              args mustBe empty
         }  
      }

      "return UnknownConnection on GET session on a bad connection" in {

         // Create a session
         val sid = newSid()
         val body = Json.obj(
            "cid" -> connId,
            "ssn" -> sessionJson.expand("sid" -> sid)
            ).toString
         val reqPut = FakeRequest(PUT, endpoint).withTextBody(body)
         val respPut = route(app, reqPut).get
         status(respPut) mustBe OK
         contentAsString(respPut) mustBe empty
         
         // Attempt to get the session a bad connection.
         val respGet = route(app, FakeRequest(GET, endpoint + "/" + scid(sid, "INVALID"))).get
         status(respGet) mustBe BAD_REQUEST
         val (isInternal, error, args) = parseError(contentAsJson(respGet))
         isInternal mustBe UnknownConnection.isInternal() 
         error mustBe UnknownConnection
         args mustBe Seq("INVALID")
                  
      }

      "return SessionExpired on GET session on a wrong connection" in {

         // Create a session
         val sid = newSid()
         val body = Json.obj(
            "cid" -> connId,
            "ssn" -> sessionJson.expand("sid" -> sid)
            ).toString
         val reqPut = FakeRequest(PUT, endpoint).withTextBody(body)
         val respPut = route(app, reqPut).get
         status(respPut) mustBe OK
         contentAsString(respPut) mustBe empty

         // Obtain a new connection.
         val connResp = route(app, FakeRequest(POST, context + "/connection/big_covar_schema")).get
         status(connResp) mustBe OK
         val json = contentAsJson(connResp) 
         json mustNot be (null)
         val conn2Id = (json \ "id").as[String]
         conn2Id mustNot be (null)
         
         // Attempt to get the session the new connection.
         val respGet = route(app, FakeRequest(GET, endpoint + "/" + scid(sid, conn2Id))).get
         val (isInternal, error, args) = parseError(contentAsJson(respGet))
         isInternal mustBe SessionExpired.isInternal() 
         error mustBe SessionExpired
         args mustBe empty
                  
      }

      "return UnknownConnection on GET session after connection close" in {

         // Obtain a new connection.
         val connResp = route(app, FakeRequest(POST, context + "/connection/big_covar_schema")).get
         status(connResp) mustBe OK
         val json = contentAsJson(connResp) 
         json mustNot be (null)
         val conn2Id = (json \ "id").as[String]
         conn2Id mustNot be (null)
         
         // Create a session on it
         val sid = newSid()
         val body = Json.obj(
            "cid" -> connId,
            "ssn" -> sessionJson.expand("sid" -> sid)
            ).toString
         val reqPut = FakeRequest(PUT, endpoint).withTextBody(body)
         val respPut = route(app, reqPut).get
         status(respPut) mustBe OK
         contentAsString(respPut) mustBe empty

         // Close the connection
         val respDel = route(app, FakeRequest(DELETE, context + "/connection/" + conn2Id).withHeaders("Content-Type" -> "text/plain")).get
         status(respDel) mustBe OK
         contentAsString(respDel) mustBe empty

         // Attempt to get the session the session on the closed connection.
         val respGet = route(app, FakeRequest(GET, endpoint + "/" + scid(sid,conn2Id))).get
         val (isInternal, error, args) = parseError(contentAsJson(respGet))
         isInternal mustBe UnknownConnection.isInternal() 
         error mustBe UnknownConnection
         args mustBe Seq(conn2Id)
                  
      }

      "deserialize payload into session object" in {
       
         val sid = newSid()
         val ts = System.currentTimeMillis()
         val body = Json.obj(
            "cid" -> connId,
            "ssn" -> sessionJson.expand("sid" -> sid, "ts" -> ts)
            ).toString
         val reqPut = FakeRequest(PUT, endpoint).withTextBody(body)
         val respPut = route(app, reqPut).get
         status(respPut) mustBe OK
         contentAsString(respPut) mustBe empty
         val ssnJson = connStore.get(connId).get.getSession(sid).get.json
         ssnJson mustBe sessionJson.expand("sid" -> sid, "ts" -> ts)
         val ssn = connStore.get(connId).get.getSession(sid).get
         ssn.createDate.getTime mustBe ts
         ssn.getId mustBe sid
      
         Thread.sleep(2000);
         connStore.get(connId).get.getSession(sid) mustBe empty
         
      }
   }
}
