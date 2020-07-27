package com.github.anshulbajpai.scala_play_effect

import akka.stream.Materializer
import org.scalatest.MustMatchers._
import play.api.http.Writeable
import play.api.libs.json.JsValue
import play.api.mvc.{ EssentialAction, Request }
import play.api.test.Helpers.{ call, contentAsJson, status, _ }

@SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
object ActionTestHelpers extends {

  def executeAndAssertStatusWithContent[A](
    action: EssentialAction,
    expectedStatus: Int,
    expectedContent: JsValue
  )(implicit request: Request[A], writeable: Writeable[A], materialize: Materializer) = {
    val result = call(action, request)
    status(result)        mustEqual expectedStatus
    contentAsJson(result) mustEqual expectedContent
  }

  def executeAndAssertStatus[A](
    action: EssentialAction,
    expectedStatus: Int
  )(implicit request: Request[A], writeable: Writeable[A], materialize: Materializer) = {
    val result = call(action, request)
    status(result) mustEqual expectedStatus
  }

}
