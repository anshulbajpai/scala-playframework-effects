package tools

import cats.Id
import cats.effect.IO

import scala.concurrent.Future

trait ToFuture[F[_]] {
  def toFuture[T](t: F[T]): Future[T]
}

object ToFuture {

  def apply[F[_]](implicit lift: ToFuture[F]): ToFuture[F] = lift

  object Ops {

    implicit class ToFutureOps[F[_] : ToFuture, T](target: F[T]) {
      def toFuture: Future[T] = ToFuture[F].toFuture(target)
    }

  }

  implicit object ioToFuture extends ToFuture[IO] {
    override def toFuture[T](t: IO[T]): Future[T] = t.unsafeToFuture()
  }

  implicit object idToFuture extends ToFuture[Id] {
    override def toFuture[T](t: Id[T]): Future[T] = Future.successful(t)
  }

}