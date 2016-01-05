

package com.variant.server.dispatch

import net.liftweb.http.rest.RestHelper


/**
 * @author Igor
 * 
 */
object Dispatcher extends RestHelper {
  
  serve {
    case "my" :: "sample" :: _ Get _ => <b>Hello World</b>
  }

}
