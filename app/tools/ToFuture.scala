package tools

import cats.effect.IO

import scala.concurrent.Future

trait ToFuture[F[_]] {
  def toFuture[T](t: F[T]): Future[T]
}

object ToFuture {

  implicit object ioToFuture extends ToFuture[IO] {
    override def toFuture[T](t: IO[T]): Future[T] = t.unsafeToFuture()
  }

  def apply[F[_]](implicit lift: ToFuture[F]): ToFuture[F] = lift

  object Ops {

    implicit class ToFutureOps[F[_] : ToFuture, T](target: F[T]) {
      def toFuture: Future[T] = ToFuture[F].toFuture(target)
    }

  }

}