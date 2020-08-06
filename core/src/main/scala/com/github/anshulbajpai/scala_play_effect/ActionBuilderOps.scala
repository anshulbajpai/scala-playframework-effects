package com.github.anshulbajpai.scala_play_effect

import cats.arrow.FunctionK
import cats.implicits._
import cats.{ Functor, Id, ~> }
import play.api.mvc.{ Action, ActionBuilder }

import scala.concurrent.Future

object ActionBuilderOps {

  import ToResult.ops._

  implicit val idToFuture: Id ~> Future         = λ[Id ~> Future](a => Future.successful(a))
  implicit val futureToFuture: Future ~> Future = FunctionK.id[Future]

  implicit class ActionBuilderOps[+R[_], B](target: ActionBuilder[R, B]) {

    def sync[S: ToResult](block: R[B] => Id[S]): Action[B] = asyncF(block)

    def asyncF[F[_]: Functor, S: ToResult](
      block: R[B] => F[S]
    )(implicit fk: F ~> Future): Action[B] =
      target.async { request =>
        fk(block(request).map(_.toResult))
      }
  }

}
