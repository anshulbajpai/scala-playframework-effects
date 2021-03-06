package com.github.anshulbajpai.scala_play_effect

import cats.syntax.either._
import cats.syntax.option.none
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.json.{Json, OWrites}
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._

@SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements", "org.wartremover.warts.Any"))
class SyncActionSpecs extends ActionSpecsHelper with AnyWordSpecLike {

  import ActionBuilderOps._

  "sync" when {
    val error        = "App Error"
    val messageValue = "App Message"
    "used with a request" should {
      implicit val request =
        FakeRequest().withJsonBody(Json.obj("message" -> messageValue, "error" -> error))
      "return error when block returns Either.Left" in {
        val action = Action(json).sync { req =>
          ActionError((req.body \ "error").as[String]).asLeft[Unit]
        }
        executeAndAssertStatusWithContent(action, BAD_REQUEST, Json.obj("error" -> error))
      }
      "return ok JSON response when the action block returns Either.Right" in {
        val action = Action(json).sync { req =>
          ActionMessage((req.body \ "message").as[String]).asRight[ActionError]
        }
        executeAndAssertStatusWithContent(action, OK, Json.obj("message" -> messageValue))
      }
      "return no content response when the action block returns Unit in Either.Right" in {
        val action = Action(json).sync { _: Request[_] =>
          ().asRight[ActionError]
        }
        executeAndAssertStatus(action, NO_CONTENT)
      }

      "return not found response when the action block returns None" in {
        val action = Action.sync { _ =>
          none[String]
        }
        executeAndAssertStatus(action, NOT_FOUND)
      }

      "return ok when the action block returns Some" in {
        val action = Action(json).sync { req =>
          Some(ActionMessage((req.body \ "message").as[String]))
        }
        executeAndAssertStatusWithContent(action, OK, Json.obj("message" -> messageValue))
      }


      "return ok JSON response when the action block returns a non Either type" in {
        val action = Action(json).sync { req =>
          ActionMessage((req.body \ "message").as[String])
        }
        executeAndAssertStatusWithContent(action, OK, Json.obj("message" -> messageValue))
      }
      "return no content response when the action block returns Unit" in {
        val action = Action(json).sync { _: Request[_] =>
          ()
        }
        executeAndAssertStatus(action, NO_CONTENT)
      }
      "return Result as it is" in {
        val action = Action(json).sync { req =>
          Results.Ok(Json.toJson(ActionMessage((req.body \ "message").as[String])))
        }
        executeAndAssertStatusWithContent(action, OK, Json.obj("message" -> messageValue))
      }
    }
  }

  case class ActionMessage(message: String)

  implicit val messageWrites: OWrites[ActionMessage] = Json.writes[ActionMessage]

  case class ActionError(error: String)

  implicit val errorWrites: OWrites[ActionError] = Json.writes[ActionError]

  implicit val actionErrorToResult: ToResult[ActionError] = new ToResult[ActionError] {
    override def toResult(error: ActionError): Result = Results.BadRequest(Json.toJson(error))
  }

}
