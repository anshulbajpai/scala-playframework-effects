package com.github.anshulbajpai.scala_play_effect

import akka.actor.ActorSystem
import org.scalatest.matchers.must.Matchers._
import play.api.http.Writeable
import play.api.libs.json.JsValue
import play.api.mvc.{ DefaultActionBuilder, EssentialAction, PlayBodyParsers, Request }
import play.api.test.Helpers.{ call, contentAsJson, status, _ }

@SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements"))
trait ActionSpecsHelper {

  private implicit val system = ActorSystem()
  implicit val ec             = system.dispatcher
  private val bodyParsers     = PlayBodyParsers()

  val json = bodyParsers.json

  val Action = DefaultActionBuilder(bodyParsers.default)

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

}
