package bootstrap.routes

import bootstrap.controllers.controllers
import play.api.routing.Router
import play.api.routing.sird._

trait routes extends controllers {
  lazy val router: Router = Router.from {
    case GET(p"/async/hello/$name") => asyncHelloController.hello5(name)
    case GET(p"/sync/hello/$name") => syncHelloController.hello5(name)
  }
}
