package controllers

import cats.effect.IO
import javax.inject._
import play.api.libs.json.{Json, OWrites}
import play.api.mvc._
import services.HelloService
import services.HelloService.Message
import tools.ActionBuilderOps._
import tools.ToResult

@Singleton
class AsyncHelloController @Inject()(val controllerComponents: ControllerComponents, helloService: HelloService) extends BaseController {

  def hello1(name: String): Action[AnyContent] = Action.asyncF { implicit request: Request[AnyContent] =>
    helloService.helloF(name) // OK JSON or Errors
  }

  def hello2(name: String): Action[AnyContent] = Action.asyncF { implicit request: Request[AnyContent] =>
    helloService.helloF(name).map(_.map(_ => ())) // NO Content  or Errors
  }

  def hello3(name: String): Action[AnyContent] = Action.asyncF { implicit request: Request[AnyContent] =>
    IO.pure(Message("Hello World")) // OK JSON
  }

  def hello4: Action[AnyContent] = Action.asyncF { implicit request: Request[AnyContent] =>
    IO.unit // NO Content
  }

  def hello5(name: String): Action[AnyContent] = Action.asyncF { implicit request: Request[AnyContent] =>
    IO.pure(TempMessage(s"Temp hello $name")) // Multi result
  }

  case class TempMessage(value: String)

  implicit val messageToResult: ToResult[TempMessage] = new ToResult[TempMessage] {
    override def toResult(s: TempMessage): Result = s match {
      case m if m.value.contains("created") => Created(Json.toJson(m))
      case _ => NoContent
    }
  }

  implicit val messageWrites: OWrites[Message] = Json.writes[Message]
  implicit val tempMessageWrites: OWrites[TempMessage] = Json.writes[TempMessage]
}






