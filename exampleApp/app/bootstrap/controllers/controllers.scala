package bootstrap.controllers

import bootstrap.services.services
import cats.effect.IO
import com.softwaremill.macwire.wire
import controllers.{ AsyncHelloController, SyncHelloController }
import play.api.mvc.ControllerComponents

trait controllers extends services {
  def controllerComponents: ControllerComponents
  lazy val asyncHelloController = wire[AsyncHelloController[IO]]
  lazy val syncHelloController  = wire[SyncHelloController[IO]]
}
