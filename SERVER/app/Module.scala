import com.google.inject.AbstractModule
import java.time.Clock
import play.api.Configuration
import play.api.Environment
import play.api.Play
import com.variant.server.boot.VariantServer
import com.variant.server.boot.VariantServerImpl
import com.variant.server.conn.SessionStore
import play.api.routing.Router

/**
 * This class is a Guice module that tells Guice how to bind several
 * different types. This Guice module is created when the Play
 * application starts.

 * Play will automatically use any class called `Module` that is in
 * the root package. You can create modules in other locations by
 * adding `play.modules.enabled` settings to the `application.conf`
 * configuration file.
 */
class Module (environment: Environment, config: Configuration) extends AbstractModule {

   override def configure() = {

      // Connection Store
      //bind(classOf[ConnectionStore]).to(classOf[ConnectionStoreImpl]).asEagerSingleton

      // Session Store
      //bind(classOf[SessionStore]).to(classOf[SessionStoreImpl]).asEagerSingleton

      // Application injector
      //bind(classOf[ApplicationInjector]).to(classOf[PlayApplicationInjector]).asEagerSingleton

      // Variant server boot and shutdown
      bind(classOf[VariantServer]).to(classOf[VariantServerImpl]).asEagerSingleton

  }

}
