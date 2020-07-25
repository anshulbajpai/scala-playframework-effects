package com.github.anshulbajpai.scalaPlayEff

import cats.Functor
import cats.implicits._
import play.api.mvc.{ Action, ActionBuilder, AnyContent, BodyParser }

object ActionBuilderOps {

  import ToFuture.ops._
  import ToResult.ops._

  class ActionBuilderF[F[_], +R[_], B](target: ActionBuilder[R, B]) {
    def apply[S: ToResult](
      block: => F[S]
    )(implicit toFuture: ToFuture[F], functor: Functor[F]): Action[AnyContent] =
      target.async {
        block.map(_.toResult).toFuture
      }

    def apply[S: ToResult](
      block: R[B] => F[S]
    )(implicit toFuture: ToFuture[F], functor: Functor[F]): Action[B] =
      target.async { request: R[B] =>
        block(request).map(_.toResult).toFuture
      }

    def apply[S: ToResult, A](
      bodyParser: BodyParser[A]
    )(block: R[A] => F[S])(implicit toFuture: ToFuture[F], functor: Functor[F]): Action[A] =
      target.async(bodyParser) { request: R[A] =>
        block(request).map(_.toResult).toFuture
      }
  }

  implicit class ActionBuilderOps[+R[_], B](target: ActionBuilder[R, B]) {
    def asyncF[F[_]] = new ActionBuilderF[F, R, B](target)
  }

}
