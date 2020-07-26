package bootstrap.routes

import bootstrap.controllers.controllers
import play.api.routing.Router
import play.api.routing.sird._

trait routes extends controllers {
  lazy val router: Router = Router.from {
    case GET(p"/async/hello/$name") => helloController.hello5(name)
  }
}
