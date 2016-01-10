

package com.variant.server.dispatch

import net.liftweb.http.rest.RestHelper
import net.liftweb.http.Req
import net.liftweb.json.JsonAST.JValue


/**
 * @author Igor
 * 
 */
object Dispatcher extends RestHelper {
  
   // Remember about the prefix helper!
   
  serve {
    case "hello" :: "world" :: _ Get _ => <b>Hello World</b>
    case "event" :: "new" ::  Nil JsonPost json -> req => postArticle(json)
  }

  //
  def postArticle(jsonData: JValue): JValue = {
    import net.liftweb.json._
    import net.liftweb.json.JsonDSL._

    jsonData    
  }   
  
  def createEvent(req: Req): String = "event"
  
}
