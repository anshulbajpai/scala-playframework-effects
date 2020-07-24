package controllers

import com.github.anshulbajpai.scalaPlayEff.ActionBuilderOps._
import com.github.anshulbajpai.scalaPlayEff.ToResult
import play.api.libs.json.{ Json, OWrites }
import play.api.mvc._
import services.HelloService
import services.HelloService.Message

class SyncHelloController[F[_]](
  val controllerComponents: ControllerComponents,
  helloService: HelloService[F]
) extends BaseController {

  def hello1(name: String): Action[AnyContent] = Action.sync {
    helloService.hello(name) // OK JSON or Errors
  }

  def hello2(name: String): Action[AnyContent] = Action.sync {
    helloService.hello(name).map(_ => ()) // NO Content  or Errors
  }

  def hello3: Action[AnyContent] = Action.sync {
    Message("Hello World") // OK JSON
  }

  def hello4: Action[AnyContent] = Action.sync {
    () // NO Content
  }

  def hello5(name: String): Action[AnyContent] = Action.sync {
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
