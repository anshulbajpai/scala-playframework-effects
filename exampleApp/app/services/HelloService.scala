package services

import cats.Applicative
import error.AppError
import error.AppError.{ BadUserName, UserNotFound }
import services.HelloService.Message

class HelloService[F[_]: Applicative] {
  def helloF(name: String): F[Either[AppError, Message]] = Applicative[F].pure(hello(name))

  def hello(name: String): Either[AppError, Message] = name match {
    case name if name.toLowerCase.contains("notfound") =>
      Left(UserNotFound(s"$name user not found"))
    case name if name.toLowerCase.contains("badname") => Left(BadUserName(s"$name is a bad name"))
    case _ => Right(Message(s"hello $name"))
  }
}

object HelloService {
  case class Message(value: String)
}
