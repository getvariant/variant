package com.variant.server.test.controller

import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._
import scala.collection.JavaConversions._
import com.variant.server.test.util.ParameterizedString
import com.variant.server.api.ConfigKeys
import com.variant.server.test.BaseSpecWithServer
import com.variant.core.ServerError._
import com.variant.core.util.StringUtils
import play.api.libs.json._
import com.variant.server.impl.SessionImpl


/**
 * Session Controller Tests
 */
object SessionTest {
   val sessionJsonBigCovarPrototype = """
      {"sid":"${sid:SID}",
       "ts": ${ts:%d}, 
       "request": {"state": "state1","status": "OK","committed": true, 
                  "params": [{"name": "PARAM ONE", "value": "Param One Value"},{"name": "PARAM TWO", "value": "Param Two Value"}], 
                  "exps": ["test1.A.true","test2.B.false","test3.C.false"]},
        "states": [{"state": "state1","count": 23}, {"state": "state2","count": 32}],
        "attrList": [{"name": "NAME1","val": "${attrValue:VALUE1}"}, {"name": "NAME2","val": "VALUE2"}],
        "tests": ["test1","test2"]
      }
   """
   val sessionJsonPetclinicPrototype = """
      {"sid":"${sid:SID}",
       "ts": ${ts:%d}, 
       "request": {"state": "newOwner","status": "OK","committed": false, 
                  "params": [{"name": "PARAM ONE", "value": "Param One Value"},{"name": "PARAM TWO", "value": "Param Two Value"}]},
        "attrList": [{"name": "NAME1","val": "VALUE1"}, {"name": "NAME2","val": "VALUE2"}]
      }
   """

}

class SessionTest extends BaseSpecWithServer {
   
   import SessionTest._
   
   val endpoint = context + "/session"
   
   val sessionJsonBigCovar = ParameterizedString(
         sessionJsonBigCovarPrototype.format(System.currentTimeMillis()))
         
   val sessionJsonPetclinic = ParameterizedString(
         sessionJsonPetclinicPrototype.format(System.currentTimeMillis()))
   
   val sessionTimeoutMillis = server.config.getLong(ConfigKeys.SESSION_TIMEOUT) * 1000
   sessionTimeoutMillis mustEqual 1000
   
   val vacuumIntervalMillis = server.config.getLong(ConfigKeys.SESSION_VACUUM_INTERVAL) * 1000
   vacuumIntervalMillis  mustEqual 1000


   "SessionController" should {

      "return 404 on GET no SID" in {
         
         val resp = route(app, FakeRequest(GET, endpoint)).get
         status(resp) mustBe NOT_FOUND
         contentAsString(resp) mustBe empty
      }

      var connId: String = null 
      var connSchid: String = null
      
      "obtain a connection" in {
         // POST new connection
         val connResp = route(app, connectionRequest("big_covar_schema")).get
         status(connResp) mustBe OK
         val json = contentAsJson(connResp) 
         json mustNot be (null)
         connId = (json \ "id").as[String]
         connId mustNot be (null)
         connSchid = (json \ "schema" \ "id").as[String]
      }

      "return SessionExpired on GET non-existent session" in {  
         
         val resp = route(app, connectedRequest(GET, endpoint + "/foo", connId)).get
         status(resp) mustBe BAD_REQUEST
         val (isInternal, error, args) = parseError(contentAsJson(resp))
         isInternal mustBe false 
         error mustBe SessionExpired
         args mustBe Seq("foo")
      }

      
      "return OK on PUT non-existent session with valid schema ID" in {
         
         val body = sessionJsonBigCovar.expand("sid" -> "foo")
         val resp = route(app, connectedRequest(PUT, endpoint, connId).withTextBody(body)).get
         status(resp) mustBe OK
         contentAsString(resp) mustBe empty
      }

      "return OK and existing session on GET" in {
       
         val resp = route(app, connectedRequest(GET, endpoint + "/foo", connId)).get
         status(resp) mustBe OK
         val respAsJson = contentAsJson(resp)
         StringUtils.digest((respAsJson \ "session").as[String]) mustBe 
            StringUtils.digest(sessionJsonBigCovar.expand("sid" -> "foo").toString())
      }

      "return OK and replace existing session on PUT" in {
       
         val body = sessionJsonBigCovar.expand("sid" -> "foo")
         val reqPut = connectedRequest(PUT, endpoint, connId).withTextBody(body)
         val respPut = route(app, reqPut).get
         status(respPut) mustBe OK
         contentAsString(respPut) mustBe empty
         
         val respGet = route(app, connectedRequest(GET, endpoint + "/foo", connId)).get
         status(respGet) mustBe OK
         val respAsJson = contentAsJson(respGet)
         StringUtils.digest((respAsJson \ "session").as[String]) mustBe 
            StringUtils.digest(sessionJsonBigCovar.expand("sid" -> "foo"))
      }

      "return OK and create session on PUT" in {
       
         val body = sessionJsonBigCovar.expand("sid" -> "bar1")
         val reqPut = connectedRequest(PUT, endpoint, connId).withTextBody(body)
         val respPut = route(app, reqPut).get
         status(respPut) mustBe OK
         contentAsString(respPut) mustBe empty
         
         val respGet = route(app, connectedRequest(GET, endpoint + "/bar1", connId)).get
         status(respGet) mustBe OK
         val respAsJson = contentAsJson(respGet)
         StringUtils.digest((respAsJson \ "session").as[String]) mustBe 
            StringUtils.digest(sessionJsonBigCovar.expand("sid" -> "bar1"))
      }

     "not lose existing session with different key" in {

         val resp = route(app, connectedRequest(GET, endpoint + "/foo", connId)).get
         status(resp) mustBe OK
         val respAsJson = contentAsJson(resp)
         StringUtils.digest((respAsJson \ "session").as[String]) mustBe 
            StringUtils.digest(sessionJsonBigCovar.expand("sid" -> "foo"))
      }

      "keep an existing session alive" in {
      
         val halfExp = sessionTimeoutMillis / 2
         halfExp mustBe 500   
         for ( wait <- Seq(halfExp, halfExp, halfExp, halfExp) ) {
            Thread.sleep(wait)
            val resp = route(app, connectedRequest(GET, endpoint + "/foo", connId)).get
            status(resp) mustBe OK
            val respAsJson = contentAsJson(resp)
            StringUtils.digest((respAsJson \ "session").as[String]) mustBe 
               StringUtils.digest(sessionJsonBigCovar.expand("sid" -> "foo").toString())
         }
      }

      "return SessionExpired on GET expired session" in {
         
         Thread.sleep(sessionTimeoutMillis + vacuumIntervalMillis);
      
         ("foo" :: "bar" :: Nil).foreach { sid =>
              val resp = route(app, connectedRequest(GET, endpoint + "/" + sid, connId)).get
              status(resp) mustBe BAD_REQUEST
              val (isInternal, error, args) = parseError(contentAsJson(resp))
              isInternal mustBe SessionExpired.isInternal() 
              error mustBe SessionExpired
              args mustBe Seq(sid)
         }
      }

      "return OK on GET or PUT session on a parallel connection" in {

         // Create a session
         val sid = newSid()
         val body = sessionJsonBigCovar.expand("sid" -> sid)
         val reqPut = connectedRequest(PUT, endpoint, connId).withTextBody(body)
         val respPut = route(app, reqPut).get
         status(respPut) mustBe OK
         contentAsString(respPut) mustBe empty

         // Obtain a parallel connection.
         val connResp = route(app, connectionRequest("big_covar_schema")).get
         status(connResp) mustBe OK
         val json = contentAsJson(connResp) 
         json mustNot be (null)
         val conn2Id = (json \ "id").as[String]
         conn2Id mustNot be (null)
         val conn2Schid = (json \ "schema" \ "id").as[String]
         conn2Schid mustBe connSchid
         
         // Save the session on the parallel connection.
         val body2 = sessionJsonBigCovar.expand("sid" -> sid, "attrValue" -> "something else")
         val reqPut2 = connectedRequest(PUT, endpoint, conn2Id).withTextBody(body2)
         val respPut2 = route(app, reqPut2).get
         status(respPut2) mustBe OK
         contentAsString(respPut) mustBe empty

         // Get the session on the parallel connection.
         val respGet = route(app, connectedRequest(GET, endpoint + "/" + sid, conn2Id)).get
         status(respGet) mustBe OK
         val ssnJson = contentAsJson(respGet) 
         StringUtils.digest((ssnJson \ "session").as[String]) mustBe 
            StringUtils.digest(sessionJsonBigCovar.expand("sid" -> sid, "attrValue" -> "something else"))

      }

      "return SessionExpired on GET or PUT session on a wrong connection" in {

         // Create a new session on the existing big_covar_schema connection
         val sid = newSid()
         val body = sessionJsonBigCovar.expand("sid" -> sid)
         val respPut = route(app, connectedRequest(PUT, endpoint, connId).withTextBody(body)).get
         status(respPut) mustBe OK
         contentAsString(respPut) mustBe empty

         // Obtain a connection to a different schema
         val connResp = route(app, connectionRequest("petclinic")).get
         status(connResp) mustBe OK
         val json = contentAsJson(connResp) 
         json mustNot be (null)
         val conn2Id = (json \ "id").as[String]
         conn2Id mustNot be (null)
         conn2Id mustNot be (connId)
            
         // Attempt to save the session on the new connection should throw internal error.
         val body2 = sessionJsonPetclinic.expand("sid" -> sid)
         val respPut2 = route(app, connectedRequest(PUT, endpoint, conn2Id).withTextBody(body2)).get
         status(respPut2) mustBe BAD_REQUEST
         val (isInternal, error, args) = parseError(contentAsJson(respPut2))
         isInternal mustBe false 
         error mustBe SessionExpired
         args mustBe Seq(sid)

      }

      "deserialize payload into session object" in {
       
         val sid = newSid()
         val ts = System.currentTimeMillis()
         val body = sessionJsonBigCovar.expand("sid" -> sid, "ts" -> ts)
         val reqPut = connectedRequest(PUT, endpoint, connId).withTextBody(body)
         val respPut = route(app, reqPut).get
         status(respPut) mustBe OK
         contentAsString(respPut) mustBe empty
         val ssnJson = ssnStore.get(sid, connId).get.asInstanceOf[SessionImpl].toJson
         
         ssnJson mustBe normalJson(sessionJsonBigCovar.expand("sid" -> sid, "ts" -> ts))
         val ssn = ssnStore.get(sid, connId).get
         ssn.getCreateDate.getTime mustBe ts
         ssn.getId mustBe sid
      
         Thread.sleep(2000);
         ssnStore.get(sid, connId) mustBe empty
         
      }

      "return OK on GET session after connection close" in {

         // Obtain a new connection.
         val connResp = route(app, connectionRequest("big_covar_schema")).get
         status(connResp) mustBe OK
         val json = contentAsJson(connResp) 
         json mustNot be (null)
         val connId = (json \ "id").as[String]
         connId mustNot be (null)
         
         // Create a session on it
         val sid = newSid()
         val body = sessionJsonBigCovar.expand("sid" -> sid)
         val reqPut = connectedRequest(PUT, endpoint, connId).withTextBody(body)
         val respPut = route(app, reqPut).get
         status(respPut) mustBe OK
         contentAsString(respPut) mustBe empty

         // Close the connection
         val respDel = route(app, connectedRequest(DELETE, context + "/connection", connId)).get
         status(respDel) mustBe OK
         contentAsString(respDel) mustBe empty

         // Session should be alive after connection closed.
         val respGet = route(app, connectedRequest(GET, endpoint + "/" + sid, connId)).get
         status(respGet) mustBe OK

         // Session should be expired as normal.
         Thread.sleep(sessionTimeoutMillis + vacuumIntervalMillis);

         val respGet2 = route(app, connectedRequest(GET, endpoint + "/" + sid, connId)).get
         status(respGet2) mustBe BAD_REQUEST
         val (isInternal, error, args) = parseError(contentAsJson(respGet2))
         isInternal mustBe false 
         error mustBe SessionExpired
         args mustBe Seq(sid)
      }

   }
}
