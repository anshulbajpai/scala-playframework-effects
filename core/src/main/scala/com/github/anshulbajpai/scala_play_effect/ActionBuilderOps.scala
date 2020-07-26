package com.github.anshulbajpai.scala_play_effect

import cats.implicits._
import cats.{ Functor, Id }
import play.api.mvc.{ Action, ActionBuilder }

object ActionBuilderOps {

  import ToFuture.ops._
  import ToResult.ops._

  implicit class ActionBuilderOps[+R[_], B](target: ActionBuilder[R, B]) {

    def sync[S: ToResult](block: R[B] => Id[S]): Action[B] = asyncF(block)

    def asyncF[F[_]: ToFuture: Functor, S: ToResult](
      block: R[B] => F[S]
    )(implicit d: DummyImplicit): Action[B] =
      target.async { request =>
        block(request).map(_.toResult).toFuture
      }
  }

}
