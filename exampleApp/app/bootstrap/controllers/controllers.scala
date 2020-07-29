package bootstrap.controllers

import bootstrap.services.services
import cats.effect.IO
import cats.~>
import com.softwaremill.macwire.wire
import controllers.HelloController
import play.api.mvc.ControllerComponents

import scala.concurrent.Future

trait controllers extends services {
  def controllerComponents: ControllerComponents

  implicit val ioToFuture = λ[IO ~> Future](_.unsafeToFuture())

  lazy val helloController = wire[HelloController[IO]]
}
