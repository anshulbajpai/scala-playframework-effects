package bootstrap.controllers

import bootstrap.services.services
import com.softwaremill.macwire.wire
import controllers.{ AsyncHelloController, SyncHelloController }
import play.api.mvc.ControllerComponents

trait controllers extends services {
  def cc: ControllerComponents
  lazy val asyncHelloController = wire[AsyncHelloController]
  lazy val syncHelloController  = wire[SyncHelloController]
}
