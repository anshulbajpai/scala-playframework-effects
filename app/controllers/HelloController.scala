package controllers

import cats.effect.IO
import error.AppError
import error.AppError.{BadUserName, UserNotFound}
import javax.inject._
import play.api.libs.json.{Json, OWrites}
import play.api.mvc._

@Singleton
class HelloController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {

  import tools.ActionBuilderOps._

  def hello(name: String): Action[AnyContent] = Action.asyncF { implicit request: Request[AnyContent] =>
    service(name)
  }

  case class Message(value: String)

  implicit val messageWrites: OWrites[Message] = Json.writes[Message]

  private def service(name: String): IO[Either[AppError, Message]] = {
    val result = name match {
      case name if name.toLowerCase.contains("notfound") => Left(UserNotFound(s"$name user not found"))
      case name if name.toLowerCase.contains("badname") => Left(BadUserName(s"$name is a bad name"))
      case _ => Right(Message(s"hello $name"))
    }
    IO.pure(result)
  }
}






