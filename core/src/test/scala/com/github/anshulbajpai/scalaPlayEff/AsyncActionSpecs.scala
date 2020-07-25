package com.github.anshulbajpai.scalaPlayEff

import cats.effect.IO
import cats.instances.future._
import cats.syntax.either._
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Writeable
import play.api.libs.json.{ JsValue, Json, OWrites }
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

@SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
class AsyncActionSpecs extends PlaySpec with GuiceOneAppPerSuite {

  implicit lazy val materializer = app.materializer
  implicit lazy val Action       = app.injector.instanceOf(classOf[DefaultActionBuilder])

  import ActionBuilderOps._

  "asyncF" when {
    val error        = "App Error"
    val messageValue = "App Message"
    "used with no request" should {
      implicit val request = FakeRequest()
      "return error when block returns Either.Left" in {
        val action = Action.asyncF {
          IO.pure(ActionError(error).asLeft[Unit])
        }
        executeAndAssertStatusWithContent(action, BAD_REQUEST, Json.obj("error" -> error))
      }
      "return ok JSON response when the action block returns Either.Right" in {
        val action = Action.asyncF {
          IO.pure(ActionMessage(messageValue).asRight[ActionError])
        }
        executeAndAssertStatusWithContent(action, OK, Json.obj("message" -> messageValue))
      }
      "return no content response when the action block returns Unit in Either.Right" in {
        val action = Action.asyncF {
          IO.pure(().asRight[ActionError])
        }
        executeAndAssertStatus(action, NO_CONTENT)
      }
      "return ok JSON response when the action block returns a non Either type" in {
        val action = Action.asyncF {
          IO.pure(ActionMessage(messageValue))
        }
        executeAndAssertStatusWithContent(action, OK, Json.obj("message" -> messageValue))
      }
      "return no content response when the action block returns Unit" in {
        val action = Action.asyncF {
          IO.unit
        }
        executeAndAssertStatus(action, NO_CONTENT)
      }
      "return Result as it is" in {
        val action = Action.asyncF {
          IO.pure(Results.Ok(Json.toJson(ActionMessage(messageValue))))
        }
        executeAndAssertStatusWithContent(action, OK, Json.obj("message" -> messageValue))
      }
      "work with Future effect" in {
        implicit val ec = app.actorSystem.dispatcher
        val action = Action.asyncF {
          Future.successful(ActionMessage(messageValue).asRight[ActionError])
        }
        executeAndAssertStatusWithContent(action, OK, Json.obj("message" -> messageValue))
      }
    }
  }

  def executeAndAssertStatusWithContent[A](
    action: EssentialAction,
    expectedStatus: Int,
    expectedContent: JsValue
  )(implicit request: Request[A], writeable: Writeable[A]) = {
    val result = call(action, request)
    status(result)        mustEqual expectedStatus
    contentAsJson(result) mustEqual expectedContent
  }

  def executeAndAssertStatus[A](
    action: EssentialAction,
    expectedStatus: Int
  )(implicit request: Request[A], writeable: Writeable[A]) = {
    val result = call(action, request)
    status(result) mustEqual expectedStatus
  }

  case class ActionMessage(message: String)

  implicit val messageWrites: OWrites[ActionMessage] = Json.writes[ActionMessage]

  case class ActionError(error: String)

  implicit val errorWrites: OWrites[ActionError] = Json.writes[ActionError]

  implicit val actionErrorToResult: ToResult[ActionError] = new ToResult[ActionError] {
    override def toResult(error: ActionError): Result = Results.BadRequest(Json.toJson(error))
  }

}
