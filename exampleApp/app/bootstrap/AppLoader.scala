package bootstrap

import bootstrap.controllers.controllers
import play.api.ApplicationLoader.Context
import play.api.{ Application, ApplicationLoader, BuiltInComponentsFromContext, LoggerConfigurator }
import play.filters.HttpFiltersComponents
import router.Routes

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
    with HttpFiltersComponents { self =>

  object graph extends controllers {
    override lazy val cc = controllerComponents
  }

  import graph._

  lazy val router = new Routes(httpErrorHandler, asyncHelloController, syncHelloController)
}
