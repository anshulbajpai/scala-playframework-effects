package bootstrap

import controllers.{ AsyncHelloController, SyncHelloController }
import play.api.ApplicationLoader.Context
import play.api.{ Application, ApplicationLoader, BuiltInComponentsFromContext, LoggerConfigurator }
import play.filters.HttpFiltersComponents
import router.Routes
import services.HelloService

class AppLoader extends ApplicationLoader {
  override def load(context: ApplicationLoader.Context): Application = {
    LoggerConfigurator(context.environment.classLoader).foreach {
      _.configure(context.environment, context.initialConfiguration, Map.empty)
    }
    new AppComponents(context).application
  }
}

class AppComponents(context: Context)
    extends BuiltInComponentsFromContext(context)
    with HttpFiltersComponents {

  lazy val helloService         = new HelloService
  lazy val asyncHelloController = new AsyncHelloController(controllerComponents, helloService)
  lazy val syncHelloController  = new SyncHelloController(controllerComponents, helloService)

  lazy val router = new Routes(httpErrorHandler, asyncHelloController, syncHelloController)
}
