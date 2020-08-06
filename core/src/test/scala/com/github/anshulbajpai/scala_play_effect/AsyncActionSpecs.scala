package com.github.anshulbajpai.scala_play_effect

import cats.effect.IO
import cats.instances.future._
import cats.syntax.either._
import cats.~>
import org.scalatest.WordSpecLike
import play.api.libs.json.{ Json, Writes }
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

@SuppressWarnings(Array("org.wartremover.warts.Any"))
class AsyncActionSpecs extends ActionSpecsHelper with WordSpecLike {

  implicit val ioToFuture: IO ~> Future = λ[IO ~> Future](_.unsafeToFuture())

  import ActionBuilderOps._

  "asyncF" when {
    val errorValue   = "App Error"
    val messageValue = "App Message"

    "used with a request" should {
      implicit val request =
        FakeRequest().withJsonBody(Json.obj("message" -> messageValue, "error" -> errorValue))
      "return error when block returns Either.Left" in {
        val action = Action(json).asyncF { req =>
          IO.pure(ActionError((req.body \ "error").as[String]).asLeft[ActionMessage])
        }
        executeAndAssertStatusWithContent(action, BAD_REQUEST, Json.obj("error" -> errorValue))
      }
      "return ok JSON response when the action block returns Either.Right" in {
        val action = Action(json).asyncF { req =>
          IO.pure(ActionMessage((req.body \ "message").as[String]).asRight[ActionError])
        }
        executeAndAssertStatusWithContent(action, OK, Json.obj("message" -> messageValue))
      }
      "return no content response when the action block returns Unit in Either.Right" in {
        val action = Action(json).asyncF { _: Request[_] =>
          IO.pure(().asRight[ActionError])
        }
        executeAndAssertStatus(action, NO_CONTENT)
      }
      "return ok JSON response when the action block returns a non Either type" in {
        val action = Action(json).asyncF { req =>
          IO.pure(ActionMessage((req.body \ "message").as[String]))
        }
        executeAndAssertStatusWithContent(action, OK, Json.obj("message" -> messageValue))
      }
      "return no content response when the action block returns Unit" in {
        val action = Action(json).asyncF { _: Request[_] =>
          IO.unit
        }
        executeAndAssertStatus(action, NO_CONTENT)
      }
      "return Result as it is" in {
        val action = Action(json).asyncF { req =>
          IO.pure(Results.Ok(Json.toJson(ActionMessage((req.body \ "message").as[String]))))
        }
        executeAndAssertStatusWithContent(action, OK, Json.obj("message" -> messageValue))
      }
      "work with Future effect" in {
        val action = Action(json).asyncF { req =>
          Future.successful(ActionMessage((req.body \ "message").as[String]).asRight[ActionError])
        }
        executeAndAssertStatusWithContent(action, OK, Json.obj("message" -> messageValue))
      }
    }
  }

  case class ActionMessage(message: String)

  implicit val messageWrites: Writes[ActionMessage] = Json.writes[ActionMessage]

  case class ActionError(error: String)

  implicit val errorWrites: Writes[ActionError] = Json.writes[ActionError]

  implicit val actionErrorToResult: ToResult[ActionError] = new ToResult[ActionError] {
    override def toResult(error: ActionError): Result = Results.BadRequest(Json.toJson(error))
  }

}
