package com.variant.server.controller

import javax.inject.Inject
import play.api.mvc.Controller
import play.api.mvc.Request
import com.variant.server.conn.SessionStore
import play.api.Logger
import com.variant.core.exception.ServerError._
import play.api.libs.json._
import com.variant.server.boot.VariantServer
import com.variant.server.ConfigKeys
import com.variant.server.boot.ServerErrorRemote
import com.variant.core.session.CoreSession
import com.variant.server.conn.ConnectionStore
import com.variant.server.session.ServerSession
import com.variant.server.ServerException

//@Singleton -- Is this for non-shared state controllers?
class SessionController @Inject() (connStore: ConnectionStore, server: VariantServer) extends Controller  {
   
      private val logger = Logger(this.getClass)	

      /**
       * SCID is Session id, followed by Conn ID, separated by :
       */
      private def parseScid(sid:String) : (String,String) = {
         val tokens = sid.split("\\:")
         if (tokens.length != 2) throw new ServerException.User(InvalidSessionId, sid)
         (tokens(0),tokens(1))
      }
      
   // def index = TODO
 
   /**
    * PUT new session into the session store.
    * @param cid Connection ID
    * test with:
curl -v -H "Content-Type: text/plain; charset=utf-8" \
     -X PUT \
     -d '{"sid": "SID","ts": 1234567,"schid": "SCHID", 
          "req": {"state": "state1","status": "OK","comm": true, 
                  "params": [{"key": "KEY1", "val": "VAL1"},{"key": "KEY2", "val": "VAL2"}], 
                  "exps": ["test1.A.true","test2.B.false","test3.C.false"]},
        "states": [{"state": "state1","count": 23}, {"state": "state2","count": 32}],
        "tests": [{"test": "test1","qual": true},{"test": "test1","qual": true}]}' \
     http://localhost:9000/variant/session/SID 
    */
   def put(scid: String) = VariantAction { req =>

      val (sid,cid) = parseScid(scid)
      val conn = connStore.get(cid)

      if (!conn.isDefined) {
         logger.debug(s"Not found connection [$cid]")               
         ServerErrorRemote(UnknownConnection).asResult(cid)
      }
      else {
         logger.debug(s"Found connection [$cid]")
         req.body.asText match {
            case Some(body) => {
               conn.get.addSession(sid, body)
               Ok
            }
            case None => ServerErrorRemote(EmptyBody).asResult()
         }
      }
   }
 
   /**
    * GET a session by ID.
    * test with:
curl -v -X GET http://localhost:9000/variant/session/SID
    */
   def get(scid: String) = VariantAction {

      val (sid,cid) = parseScid(scid)
      val conn = connStore.get(cid)
      
      if (!conn.isDefined) {
         logger.debug(s"Not found connection [$cid]")               
         ServerErrorRemote(UnknownConnection).asResult(cid)
      }
      else {
         logger.debug(s"Found connection [$cid]")
         val result = conn.get.getSessionJson(sid)
         if (result.isDefined) {
            logger.debug("Session found for ID " + sid)
            val response = JsObject(Seq(
               "session" -> JsString(result.get)
            ))
            Ok(response.toString)
         }
         else {
            logger.debug(s"No session found for ID [$sid]")         
            NotFound
         }
      }
   }
 
   def post(id: String) = VariantAction {
      NotImplemented
   }
 
   def delete(id: String) = VariantAction {
      NotImplemented
   }
}
