package controllers

import cats.Applicative
import cats.syntax.functor._
import com.github.anshulbajpai.scalaPlayEff.ActionBuilderOps._
import com.github.anshulbajpai.scalaPlayEff.{ ToFuture, ToResult }
import play.api.libs.json.{ Json, OWrites }
import play.api.mvc._
import services.HelloService
import services.HelloService.Message

class AsyncHelloController[F[_]: Applicative: ToFuture](
  val controllerComponents: ControllerComponents,
  helloService: HelloService[F]
) extends BaseController {

  def hello1(name: String): Action[AnyContent] = Action.asyncF {
    helloService.helloF(name) // OK JSON or Errors
  }

  def hello2(name: String): Action[AnyContent] = Action.asyncF {
    helloService.helloF(name).map(_.map(_ => ())) // NO Content  or Errors
  }

  def hello3: Action[AnyContent] = Action.asyncF {
    Applicative[F].pure(Message("Hello World")) // OK JSON
  }

  def hello4: Action[AnyContent] = Action.asyncF {
    Applicative[F].unit // NO Content
  }

  def hello5(name: String): Action[AnyContent] = Action.asyncF {
    Applicative[F].pure(TempMessage(s"Temp hello $name")) // Multi result
  }

  def hello6(name: String): Action[AnyContent] = Action.sync {
    helloService.hello(name) // OK JSON or Errors
  }

  case class TempMessage(value: String)

  implicit val messageToResult: ToResult[TempMessage] = new ToResult[TempMessage] {
    override def toResult(s: TempMessage): Result = s match {
      case m if m.value.contains("created") => Created(Json.toJson(m))
      case _ => NoContent
    }
  }

  implicit val messageWrites: OWrites[Message]         = Json.writes[Message]
  implicit val tempMessageWrites: OWrites[TempMessage] = Json.writes[TempMessage]
}
