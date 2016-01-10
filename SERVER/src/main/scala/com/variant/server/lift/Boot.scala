package com.variant.server.lift

import net.liftweb.http.LiftRules
import com.variant.server.dispatch.Dispatcher
import net.liftweb.http.Bootable

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot extends Bootable {
  
  def boot {
    
    LiftRules.statelessDispatch.append(Dispatcher)

  }
}