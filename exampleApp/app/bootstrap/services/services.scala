package bootstrap.services

import com.softwaremill.macwire.wire
import services.HelloService

trait services {
  lazy val helloService: HelloService = wire[HelloService]
}
