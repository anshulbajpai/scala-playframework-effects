package tools

import cats.implicits._
import cats.{Functor, Id}
import play.api.mvc._

object ActionBuilderOps {

  import ToFuture.Ops._
  import ToResult.Ops._

  implicit class ActionBuilderOps[+R[_], B](target: ActionBuilder[R, B]) {

    def asyncF[F[_] : Functor : ToFuture, S: ToResult](block: R[B] => F[S]): Action[B] = target.async { implicit request: R[B] =>
      block(request).map(_.toResult).toFuture
    }

    def sync[S: ToResult](block: R[B] => S): Action[B] = asyncF[Id, S](block)
  }

}








