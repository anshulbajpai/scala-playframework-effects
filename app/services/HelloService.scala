package services

import cats.effect.IO
import error.AppError
import error.AppError.{BadUserName, UserNotFound}
import javax.inject.Singleton
import services.HelloService.Message

@Singleton
class HelloService {
  def helloF(name: String): IO[Either[AppError, Message]] = IO.pure(hello(name))

  def hello(name: String): Either[AppError, Message] = name match {
    case name if name.toLowerCase.contains("notfound") => Left(UserNotFound(s"$name user not found"))
    case name if name.toLowerCase.contains("badname") => Left(BadUserName(s"$name is a bad name"))
    case _ => Right(Message(s"hello $name"))
  }
}

object HelloService {
  case class Message(value: String)
}
