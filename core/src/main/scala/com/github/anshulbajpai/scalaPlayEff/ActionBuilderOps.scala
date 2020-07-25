package com.github.anshulbajpai.scalaPlayEff

import cats.implicits._
import cats.{ Functor, Id }
import play.api.mvc.{ Action, ActionBuilder, AnyContent }

object ActionBuilderOps {

  import ToFuture.ops._
  import ToResult.ops._

  implicit class ActionBuilderOps[+R[_], B](target: ActionBuilder[R, B]) {
    def sync[S: ToResult](block: => Id[S]): Action[AnyContent] = asyncF(block)

    def sync[S: ToResult](block: R[B] => Id[S]): Action[B] = asyncF(block)

    def asyncF[F[_]: ToFuture: Functor, S: ToResult](block: => F[S]): Action[AnyContent] =
      target.async {
        block.map(_.toResult).toFuture
      }

    def asyncF[F[_]: ToFuture: Functor, S: ToResult](block: R[B] => F[S]): Action[B] =
      target.async { request: R[B] =>
        block(request).map(_.toResult).toFuture
      }
  }

}
