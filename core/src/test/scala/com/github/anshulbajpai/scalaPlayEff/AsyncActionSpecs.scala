package com.github.anshulbajpai.scalaPlayEff

import akka.stream.Materializer
import cats.effect.IO
import cats.syntax.either._
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.mvc.{ DefaultActionBuilder, Result, Results }
import play.api.test.FakeRequest
import play.api.test.Helpers._

@SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements", "org.wartremover.warts.Any"))
class AsyncActionSpecs extends PlaySpec with GuiceOneAppPerTest {

  implicit lazy val materializer: Materializer = app.materializer
  implicit lazy val Action                     = app.injector.instanceOf(classOf[DefaultActionBuilder])

  import ActionBuilderOps._

  "async block without a request" when {
    "returns error " should {
      "return error status " in {
        val error = "Boom"
        val action = Action.asyncF {
          IO.pure(ActionError(error).asLeft[Unit])
        }
        val request = FakeRequest()
        val result  = call(action, request)
        status(result)          mustEqual BAD_REQUEST
        contentAsString(result) mustEqual error
      }
    }
  }

  case class ActionError(message: String)

  implicit val actionErrorToResult: ToResult[ActionError] = new ToResult[ActionError] {
    override def toResult(s: ActionError): Result = Results.BadRequest(s.message)
  }

}
