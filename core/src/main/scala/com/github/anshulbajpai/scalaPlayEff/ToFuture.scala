package com.github.anshulbajpai.scalaPlayEff

import cats.Id
import cats.effect.IO
import simulacrum.typeclass

import scala.concurrent.Future

@typeclass
trait ToFuture[F[_]] {
  def toFuture[T](t: F[T]): Future[T]
}

object ToFuture {
  implicit object ioToFuture extends ToFuture[IO] {
    override def toFuture[T](t: IO[T]): Future[T] = t.unsafeToFuture()
  }

  implicit object idToFuture extends ToFuture[Id] {
    override def toFuture[T](t: Id[T]): Future[T] = Future.successful(t)
  }

  implicit object futureToFuture extends ToFuture[Future] {
    override def toFuture[T](t: Future[T]): Future[T] = t
  }

}
