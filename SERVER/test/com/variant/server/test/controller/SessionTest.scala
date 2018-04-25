package com.variant.server.test.controller

import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._
import scala.collection.JavaConversions._
import com.variant.server.test.util.ParameterizedString
import com.variant.server.api.ConfigKeys
import com.variant.server.test.spec.BaseSpecWithServer
import com.variant.core.ServerError._
import com.variant.core.util.StringUtils
import play.api.libs.json._
import com.variant.server.impl.SessionImpl
import com.variant.core.util.Constants._
import com.variant.core.ConnectionStatus._

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
                  "params": [{"name": "PARAM ONE", "value": "Param One Value"},{"name": "PARAM TWO", "value": "Param Two Value"}],
                  "exps": ["NewOwnerTest.tosCheckbox.false"]},
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

      var connId: String = null 
      var connSchid: String = null
      
      "obtain a connection" in {
         // POST new connection
         assertResp(route(app, connectionRequest("big_covar_schema")))
           .isOk
           .withConnStatusHeader(OPEN)
           .withBodyJson { json =>
               connId = (json \ "id").as[String]
               connId mustNot be (null)
               connSchid = (json \ "schema" \ "id").as[String]
         }
      }

      "return SessionExpired on GET non-existent session on valid CID" in {  
         
         val body = Json.obj(
            "sid" -> "bad"
         ).toString
         
         assertResp(route(app, connectedRequest(GET, endpoint, connId).withBody(body)))
            .isError(SessionExpired, "bad")
            .withConnStatusHeader(OPEN)
      }

      "return EmptyBody on GET with valid cid and no body" in {  
                  
         assertResp(route(app, connectedRequest(GET, endpoint, connId)))
            .isError(EmptyBody)
            .withConnStatusHeader(OPEN)
      }

      "return MissingProperty on GET with valid cid and bad body" in {  
         
         val body = Json.obj(
            "sidd" -> "should have been sid"
         ).toString
         
         assertResp(route(app, connectedRequest(GET, endpoint, connId).withBody(body)))
            .isError(MissingProperty, "sid")
            .withConnStatusHeader(OPEN)
      }

      "return UnknownConnection on PUT non-existent session with invalid conn ID" in {
         
         val body = sessionJsonBigCovar.expand("sid" -> "foo")
         assertResp(route(app, connectedRequest(PUT, endpoint, "invalid").withBody(body)))
            .isError(UnknownConnection, "invalid")
            .withNoConnStatusHeader
      }


      "return OK on PUT non-existent session with valid conn ID" in {
         
         val body = sessionJsonBigCovar.expand("sid" -> "foo")
         assertResp(route(app, connectedRequest(PUT, endpoint, connId).withBody(body)))
            .isOk
            .withNoBody
            .withConnStatusHeader(OPEN)
      }

      "return OK and existing session on GET" in {
       
         val body = Json.obj(
            "sid" -> "foo"
            ).toString

         assertResp(route(app, connectedRequest(GET, endpoint, connId).withBody(body)))
            .isOk
            .withConnStatusHeader(OPEN)
            .withBodyJson { json => 
               StringUtils.digest((json \ "session").as[String]) mustBe 
                  StringUtils.digest(sessionJsonBigCovar.expand("sid" -> "foo").toString())
            }
      }

      "return OK and replace existing session on PUT" in {
       
         val putBody = sessionJsonBigCovar.expand("sid" -> "foo")
         assertResp(route(app, connectedRequest(PUT, endpoint, connId).withBody(putBody)))
            .isOk
            .withNoBody
            .withConnStatusHeader(OPEN)
         
         val getBody = Json.obj(
            "sid" -> "foo"
            ).toString

         assertResp(route(app, connectedRequest(GET, endpoint , connId).withBody(getBody)))
            .isOk
            .withConnStatusHeader(OPEN)
            .withBodyJson { json =>
               StringUtils.digest((json \ "session").as[String]) mustBe 
                  StringUtils.digest(sessionJsonBigCovar.expand("sid" -> "foo"))
            }
      }

      "return OK and create session on PUT" in {
       
         val putBody = sessionJsonBigCovar.expand("sid" -> "bar1")
         assertResp(route(app, connectedRequest(PUT, endpoint, connId).withBody(putBody)))
            .isOk
            .withNoBody
            .withConnStatusHeader(OPEN)
         
         val getBody = Json.obj(
            "sid" -> "bar1"
            ).toString

         assertResp(route(app, connectedRequest(GET, endpoint, connId).withBody(getBody)))
            .isOk
            .withConnStatusHeader(OPEN)
            .withBodyJson { json =>
               StringUtils.digest((json \ "session").as[String]) mustBe 
                  StringUtils.digest(sessionJsonBigCovar.expand("sid" -> "bar1"))
            }
      }

     "not lose existing session with different key" in {

         val body = Json.obj(
            "sid" -> "foo"
            ).toString

         assertResp(route(app, connectedRequest(GET, endpoint, connId).withBody(body)))
            .isOk
            .withConnStatusHeader(OPEN)
            .withBodyJson { json =>
               StringUtils.digest((json \ "session").as[String]) mustBe 
                  StringUtils.digest(sessionJsonBigCovar.expand("sid" -> "foo"))
            }
      }

      "keep an existing session alive" in {
      
         val body = Json.obj(
            "sid" -> "foo"
         ).toString
         
         val halfExp = sessionTimeoutMillis / 2
         halfExp mustBe 500   
         for ( wait <- Seq(halfExp, halfExp, halfExp, halfExp) ) {
            Thread.sleep(wait)
            assertResp(route(app, connectedRequest(GET, endpoint, connId).withBody(body)))
               .isOk
               .withConnStatusHeader(OPEN)
               .withBodyJson { json => 
                  StringUtils.digest((json \ "session").as[String]) mustBe 
                     StringUtils.digest(sessionJsonBigCovar.expand("sid" -> "foo").toString())
            }
         }
      }

      "return SessionExpired on GET expired session" in {
         
         Thread.sleep(sessionTimeoutMillis + vacuumIntervalMillis);
      
         ("foo" :: "bar" :: Nil).foreach { sid =>

            val body = Json.obj(
               "sid" -> sid
            ).toString

            assertResp(route(app, connectedRequest(GET, endpoint, connId).withBody(body)))
               .isError(SessionExpired, sid)
               .withConnStatusHeader(OPEN)
         }
      }

      var conn2Id: String = null

      "return OK on GET or PUT session on a parallel connection" in {

         // Create a session
         val sid = newSid()
         val body = sessionJsonBigCovar.expand("sid" -> sid)
         assertResp(route(app, connectedRequest(PUT, endpoint, connId).withBody(body)))
            .isOk
            .withConnStatusHeader(OPEN)
            .withNoBody
         
         // Obtain a parallel connection.
         assertResp(route(app, connectionRequest("big_covar_schema")))
            .isOk
            .withConnStatusHeader(OPEN)
            .withBodyJson { json => 
               conn2Id = (json \ "id").as[String]
               conn2Id mustNot be (null)
               val conn2Schid = (json \ "schema" \ "id").as[String]
               conn2Schid mustBe connSchid
            }
         
         // Save the session on the parallel connection.
         val body2 = sessionJsonBigCovar.expand("sid" -> sid, "attrValue" -> "something else")
         assertResp(route(app, connectedRequest(PUT, endpoint, conn2Id).withBody(body2)))
            .isOk
            .withNoBody
            .withConnStatusHeader(OPEN)

         Json.obj(
            "sid" -> sid
         ).toString
         
         // Get the session on the parallel connection.
         assertResp(route(app, connectedRequest(GET, endpoint, conn2Id).withBody(body)))
            .isOk
            .withConnStatusHeader(OPEN)
            .withBodyJson { json => 
               StringUtils.digest((json \ "session").as[String]) mustBe 
                  StringUtils.digest(sessionJsonBigCovar.expand("sid" -> sid, "attrValue" -> "something else"))
            }

      }

      "return SessionExpired on GET or PUT session on a wrong connection" in {

         // Create a new session on the existing big_covar_schema connection
         val sid = newSid()
         val body = sessionJsonBigCovar.expand("sid" -> sid)
         assertResp(route(app, connectedRequest(PUT, endpoint, connId).withBody(body)))
            .isOk
            .withNoBody
            .withConnStatusHeader(OPEN)

         // Obtain a connection to a different schema
         assertResp(route(app, connectionRequest("petclinic")))
            .isOk
            .withConnStatusHeader(OPEN)
            .withBodyJson { json => 
               conn2Id = (json \ "id").as[String]
               conn2Id mustNot be (null)
               conn2Id mustNot be (connId)
            }            
         
         // Attempt to save the session on the new connection should throw internal error.
         val body2 = sessionJsonPetclinic.expand("sid" -> sid)
         assertResp(route(app, connectedRequest(PUT, endpoint, conn2Id).withBody(body2)))
            .isError(SessionExpired, sid)
            .withConnStatusHeader(OPEN)
      }

      "deserialize payload into session object" in {
       
         val sid = newSid()
         val ts = System.currentTimeMillis()
         val body = sessionJsonBigCovar.expand("sid" -> sid, "ts" -> ts)
         assertResp(route(app, connectedRequest(PUT, endpoint, connId).withBody(body)))
            .isOk
            .withNoBody

         val conn = server.connStore.get(connId).get
         val ssnJson = server.ssnStore.get(sid, conn).get.asInstanceOf[SessionImpl].toJson
         ssnJson mustBe normalJson(sessionJsonBigCovar.expand("sid" -> sid, "ts" -> ts))
         val ssn = server.ssnStore.get(sid, conn).get
         ssn.getCreateDate.getTime mustBe ts
         ssn.getId mustBe sid
      
         Thread.sleep(2000);
         server.ssnStore.get(sid, conn) mustBe empty
         
      }

      var sid: String = null

      "allow access to a session after connection close on a parallel connection" in {

         assertResp(route(app, connectionRequest("big_covar_schema")))
            .isOk
            .withConnStatusHeader(OPEN)
            .withBodyJson { json =>
               connId = (json \ "id").as[String]
               connId mustNot be (null)
         }     

        assertResp(route(app, connectionRequest("big_covar_schema")))
            .isOk
            .withConnStatusHeader(OPEN)
            .withBodyJson { json =>
               conn2Id = (json \ "id").as[String]
               conn2Id mustNot be (null)
         }     

         // Create a session in cid1
         sid = newSid
         val body = sessionJsonBigCovar.expand("sid" -> sid)
         assertResp(route(app, connectedRequest(PUT, endpoint, connId).withBody(body)))
            .isOk
            .withConnStatusHeader(OPEN)
            .withNoBody

         // Close connection cid1
         assertResp(route(app, connectedRequest(DELETE, context + "/connection", connId)))
            .isOk
            .withNoBody
            .withConnStatusHeader(CLOSED_BY_CLIENT)

         val getBody = Json.obj(
            "sid" -> sid
         ).toString

         // Cannot get to session over closed connection.
         assertResp(route(app, connectedRequest(GET, endpoint, connId).withBody(getBody)))
            .isError(UnknownConnection, connId)
            .withNoConnStatusHeader

         // OK getting to session over open connection.
         assertResp(route(app, connectedRequest(GET, endpoint, conn2Id).withBody(getBody)))
            .isOk
            .withConnStatusHeader(OPEN)

       }

       "expire session as normal" in {

         Thread.sleep(sessionTimeoutMillis + vacuumIntervalMillis);

         val getBody = Json.obj(
            "sid" -> sid
         ).toString

         // Same error over the closed connection
         assertResp(route(app, connectedRequest(GET, endpoint, connId).withBody(getBody)))
            .isError(UnknownConnection, connId)
            .withNoConnStatusHeader

         // Session Expired over live connection.
         assertResp(route(app, connectedRequest(GET, endpoint , conn2Id).withBody(getBody)))
            .isError(SessionExpired, sid)
            .withConnStatusHeader(OPEN)

       }

   }
}
