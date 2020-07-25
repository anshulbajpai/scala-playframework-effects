package com.github.anshulbajpai.scalaPlayEff

import cats.Id
import cats.effect.IO
import cats.instances.future._
import cats.syntax.either._
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.{Json, OWrites}
import play.api.mvc.{DefaultActionBuilder, Result, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

@SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements", "org.wartremover.warts.Any"))
class AsyncActionSpecs extends PlaySpec with GuiceOneAppPerSuite {

  implicit lazy val materializer = app.materializer
  implicit lazy val Action       = app.injector.instanceOf(classOf[DefaultActionBuilder])

  import ActionBuilderOps._

  "async block" should {
    "return error when the action block returns Either.Left" in {
      val error = "Boom"
      val action = Action.asyncF {
        IO.pure(ActionError(error).asLeft[Unit])
      }
      val request = FakeRequest()
      val result  = call(action, request)
      status(result)          mustEqual BAD_REQUEST
      contentAsString(result) mustEqual error
    }

    "return ok JSON response when the action block returns Either.Right" in {
      val messageValue = "Boom"
      val action = Action.asyncF {
        IO.pure(ActionMessage(messageValue).asRight[ActionError])
      }
      val request = FakeRequest()
      val result  = call(action, request)
      status(result)        mustEqual OK
      contentAsJson(result) mustEqual Json.obj("message" -> messageValue)
    }

    "return no content response when the action block returns Unit in Either.Right" in {
      val action = Action.asyncF {
        IO.pure(().asRight[ActionError])
      }
      val request = FakeRequest()
      val result  = call(action, request)
      status(result) mustEqual NO_CONTENT
    }

    "return ok JSON response when the action block returns a non Either type" in {
      val messageValue = "Boom"
      val action = Action.asyncF {
        IO.pure(ActionMessage(messageValue))
      }
      val request = FakeRequest()
      val result  = call(action, request)
      status(result)        mustEqual OK
      contentAsJson(result) mustEqual Json.obj("message" -> messageValue)
    }

    "return no content response when the action block returns Unit" in {
      val action = Action.asyncF {
        IO.unit
      }
      val request = FakeRequest()
      val result  = call(action, request)
      status(result) mustEqual NO_CONTENT
    }

    "return Result as it is" in {
      val messageValue = "Boom"
      val action = Action.asyncF {
        IO.pure(Results.Ok(messageValue))
      }
      val request = FakeRequest()
      val result  = call(action, request)
      status(result)          mustEqual OK
      contentAsString(result) mustEqual messageValue
    }

    "work when effect is Future" in {
      implicit val ec  = app.actorSystem.dispatcher
      val messageValue = "Boom"
      val action = Action.asyncF {
        Future.successful(ActionMessage(messageValue).asRight[ActionError])
      }
      val request = FakeRequest()
      val result  = call(action, request)
      status(result)        mustEqual OK
      contentAsJson(result) mustEqual Json.obj("message" -> messageValue)
    }

    "work with no effect" in {
      val messageValue = "Boom"

      val action = Action.asyncF[Id] {
        ActionMessage(messageValue).asRight[ActionError]
      }
      val request = FakeRequest()
      val result  = call(action, request)
      status(result)        mustEqual OK
      contentAsJson(result) mustEqual Json.obj("message" -> messageValue)
    }
  }

  case class ActionMessage(message: String)

  implicit val messageWrites: OWrites[ActionMessage] = Json.writes[ActionMessage]

  case class ActionError(message: String)

  implicit val actionErrorToResult: ToResult[ActionError] = new ToResult[ActionError] {
    override def toResult(s: ActionError): Result = Results.BadRequest(s.message)
  }

}
