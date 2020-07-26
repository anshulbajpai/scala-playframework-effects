package error

import com.github.anshulbajpai.scala_play_effect.ToResult
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.mvc.Results.{ BadRequest, NotFound }

sealed trait AppError

object AppError {

  final case class UserNotFound(message: String) extends AppError

  final case class BadUserName(message: String) extends AppError

  implicit object appErrorResult extends ToResult[AppError] {
    override def toResult(appError: AppError): Result = appError match {
      case UserNotFound(message) =>
        NotFound(
          Json.obj(
            "error" -> message
          )
        )
      case BadUserName(message) =>
        BadRequest(
          Json.obj(
            "error" -> message
          )
        )
    }
  }
}
