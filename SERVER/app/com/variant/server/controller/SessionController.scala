package com.variant.server.controller

import javax.inject.Inject
import play.api.mvc.Controller
import play.api.mvc.Request
import com.variant.server.session.SessionStore
import play.api.Logger
import com.variant.server.boot.ServerErrorApi._

//@Singleton -- Is this for non-shared state controllers?
class SessionController @Inject() (store: SessionStore) extends Controller  {
   
      private val logger = Logger(this.getClass)	

   // def index = TODO
 
   /**
    * PUT new session into the session store.
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
   def put(id: String) = VariantAction { req =>

      // To be a text, Content-Type header has to be text and supply a charset.
      req.body.asText match {
         case Some(body) => {
            store.put(id, body)
            Ok
         }
         case None => EmptyBody.asResult()
      }
   }
 
   /**
    * GET a session by ID.
    * test with:
curl -v -X GET http://localhost:9000/variant/session/SID
    */
   def get(id: String) = VariantAction {
      val result = store.asString(id)
      if (result.isDefined) {
         logger.trace("Session found for ID " + id)
         Ok(result.get)
      }
      else {
         logger.trace(s"No session found for ID [$id]")         
         NotFound
      }
   }
 
   def post(id: String) = VariantAction {
      NotImplemented
   }
 
   def delete(id: String) = VariantAction {
      NotImplemented
   }
}
