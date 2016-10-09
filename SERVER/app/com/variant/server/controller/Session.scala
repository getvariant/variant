package com.variant.server.controller

import javax.inject.Inject
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.mvc.Request
import com.variant.server.session.SessionStore
import play.api.Logger

//@Singleton -- Is this for non-shared state controllers?
class Session @Inject() (store: SessionStore) extends Controller  {
   
      private val logger = Logger(this.getClass)	

   // def index = TODO
 
   /**
    * PUT new session into the session store.
    * test with:
curl -v -H "Content-Type: text/plain; charset=utf-8" \
     -X PUT \
     -d '{"sid": "SID","ts": 123,"schid": "SCHID", \
          "req": {"state": "state1","status": "OK","comm": true, \
                  "params": [{"key": "KEY1", "val": "VAL1"},{"key": "KEY2", "val": "VAL2"}], \
                  "exps": ["EXP1","EXP2","EXP3"]} \ 
        "states": [{"state": "state1","count": 23}, {"state": "state2","count": 32}], \
        "tests": [{"test": "test1","qual": true},{"test": "test1","qual": true}]}' \
     http://localhost:9000/variant/session/A67 
    */
   def put(id: String) = Action { req =>
      // To be a text, Content-Type header has to be text and supply a charset.
      req.body.asText match {
         case Some(body) => {
            store.put(id, body)
            Ok
         }
         case None => BadRequest("Body expected but was null");
      }
   }
 
   /**
    * GET a session by ID.
    * test with:
curl -v -X GET http://localhost:9000/variant/session/A67 
    */
   def get(id: String) = Action {
      val result = store.asString(id)
      if (result.isDefined) {
         logger.trace("Session found for ID " + id)
         Ok(result.get)
      }
      else {
         logger.trace(s"No session found for ID [$id]")         
         BadRequest(s"Session ID [$id] does not exist")
      }
   }
 
   def post(id: String) = Action {
      NotImplemented
   }
 
   def delete(id: String) = Action {
      NotImplemented
   }
}
