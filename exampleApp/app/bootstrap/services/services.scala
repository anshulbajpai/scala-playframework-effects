package bootstrap.services

import cats.effect.IO
import com.softwaremill.macwire.wire
import services.HelloService

trait services {
  lazy val helloService = wire[HelloService[IO]]
}
