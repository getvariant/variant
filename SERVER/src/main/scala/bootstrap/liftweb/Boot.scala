package bootstrap.liftweb

import net.liftweb.http.LiftRules
import com.variant.server.dispatch.Dispatcher

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot {
  def boot {
    // where to search snippet
    LiftRules.addToPackages("code")
    
    LiftRules.statelessDispatch.append(Dispatcher)



  }
}