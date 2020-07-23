package controllers

import javax.inject._
import play.api.libs.json.{Json, OWrites}
import play.api.mvc._
import services.HelloService
import services.HelloService.Message
import tools.ToResult
import tools.ActionBuilderOps._

@Singleton
class SyncHelloController @Inject()(val controllerComponents: ControllerComponents, helloService: HelloService) extends BaseController {


  def hello1(name: String): Action[AnyContent] = Action.sync { implicit request: Request[AnyContent] =>
    helloService.hello(name) // OK JSON or Errors
  }

  def hello2(name: String): Action[AnyContent] = Action.sync { implicit request: Request[AnyContent] =>
    helloService.hello(name).map(_ => ()) // NO Content  or Errors
  }

  def hello3: Action[AnyContent] = Action.sync { implicit request: Request[AnyContent] =>
    Message("Hello World") // OK JSON
  }

  def hello4: Action[AnyContent] = Action.sync { implicit request: Request[AnyContent] =>
    () // NO Content
  }

  def hello5(name: String): Action[AnyContent] = Action.sync { implicit request: Request[AnyContent] =>
    TempMessage(s"Temp hello $name") // Multi result
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






