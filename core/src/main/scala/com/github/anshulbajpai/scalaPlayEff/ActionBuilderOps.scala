package com.github.anshulbajpai.scalaPlayEff

import cats.implicits._
import cats.{ Functor, Id }
import play.api.mvc.{ Action, ActionBuilder, AnyContent, BodyParser }

object ActionBuilderOps {

  import ToFuture.ops._
  import ToResult.ops._

  implicit class AsAsync[A](target: A) {
    def asAsync: Id[A] = target
  }

  implicit class ActionBuilderOps[+R[_], B](target: ActionBuilder[R, B]) {

    def asyncF[F[_]: Functor: ToFuture, S: ToResult](block: => F[S]): Action[AnyContent] =
      target.async {
        block.map(_.toResult).toFuture
      }

    def asyncF[F[_]: Functor: ToFuture, S: ToResult](block: R[B] => F[S]): Action[B] =
      target.async { request: R[B] =>
        block(request).map(_.toResult).toFuture
      }

    def asyncF[F[_]: Functor: ToFuture, S: ToResult, A](
      bodyParser: BodyParser[A]
    )(block: R[A] => F[S]): Action[A] =
      target.async(bodyParser) { request: R[A] =>
        block(request).map(_.toResult).toFuture
      }
  }

}
