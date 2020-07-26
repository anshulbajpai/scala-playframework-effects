package bootstrap.controllers

import bootstrap.services.services
import cats.effect.IO
import com.softwaremill.macwire.wire
import controllers.HelloController
import play.api.mvc.ControllerComponents

trait controllers extends services {
  def controllerComponents: ControllerComponents
  lazy val helloController = wire[HelloController[IO]]
}
