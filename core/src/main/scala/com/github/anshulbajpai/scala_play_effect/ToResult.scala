package com.github.anshulbajpai.scala_play_effect

import play.api.libs.json.{ Json, Writes }
import play.api.mvc.{ Result, Results }
import simulacrum.typeclass

@typeclass
trait ToResult[S] { self =>
  def toResult(s: S): Result
}

object ToResult {

  import ToResult.ops._

  implicit def errorOrA[E: ToResult, A: ToResult]: ToResult[Either[E, A]] =
    new ToResult[Either[E, A]] {
      override def toResult(s: Either[E, A]): Result = s.fold(_.toResult, _.toResult)
    }

  implicit def okJsonResult[A: Writes]: ToResult[A] = new ToResult[A] {
    override def toResult(s: A): Result = Results.Ok(Json.toJson(s))
  }

  implicit object noContentResult extends ToResult[Unit] {
    override def toResult(s: Unit): Result = Results.NoContent
  }

  implicit object idResult extends ToResult[Result] {
    override def toResult(s: Result): Result = s
  }

  implicit def notFoundOrA[A: ToResult]: ToResult[Option[A]] = new ToResult[Option[A]] {
    override def toResult(s: Option[A]): Result = s.map(_.toResult).getOrElse(Results.NotFound)
  }
}
