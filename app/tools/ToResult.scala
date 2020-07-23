package tools

import play.api.libs.json.{Writes, _}
import play.api.mvc.{Result, Results}


trait ToResult[S] {
  def toResult(s: S): Result
}

object ToResult {
  def apply[R](implicit toResult: ToResult[R]): ToResult[R] = toResult

  object Ops {

    implicit class ToResultOps[R: ToResult](target: R) {
      def toResult: Result = ToResult[R].toResult(target)
    }

  }

  import Ops._

  implicit def errorOrA[E: ToResult, A: ToResult]: ToResult[Either[E, A]] = new ToResult[Either[E, A]] {
    override def toResult(s: Either[E, A]): Result = s.fold(_.toResult, _.toResult)
  }

  implicit def okJsonResult[A: Writes]: ToResult[A] = new ToResult[A] {
    override def toResult(s: A): Result = Results.Ok(Json.toJson(s))
  }

  implicit object noContentResult extends ToResult[Unit] {
    override def toResult(s: Unit): Result = Results.NoContent
  }
}