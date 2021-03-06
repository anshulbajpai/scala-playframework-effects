# scala-playframework-effects

Scala [playframework](https://github.com/playframework/playframework) Action builders on batteries.

[![License](https://img.shields.io/hexpm/l/apa?style=plastic)](https://github.com/anshulbajpai/scala-playframework-effects/blob/master/LICENSE)

## Versions

### Play 2.8.x

[![Build Status](https://github.com/anshulbajpai/scala-playframework-effects/workflows/build/badge.svg?branch=playframework-2.8.x)](https://github.com/anshulbajpai/scala-playframework-effects/actions?query=workflow%3Abuild+branch%3Aplayframework-2.8.x)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.anshulbajpai/scala-playframework-effects_2.13/2.8.1.1?label=maven)](https://search.maven.org/artifact/com.github.anshulbajpai/scala-playframework-effects_2.13/2.8.1.1/jar)

### Play 2.7.x

[![Build Status](https://github.com/anshulbajpai/scala-playframework-effects/workflows/build/badge.svg?branch=playframework-2.7.x)](https://github.com/anshulbajpai/scala-playframework-effects/actions?query=workflow%3Abuild+branch%3Aplayframework-2.7.x)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.anshulbajpai/scala-playframework-effects_2.13/2.7.1.1?label=maven)](https://search.maven.org/artifact/com.github.anshulbajpai/scala-playframework-effects_2.13/2.7.1.1/jar)

### Play 2.6.x

[![Build Status](https://github.com/anshulbajpai/scala-playframework-effects/workflows/build/badge.svg?branch=playframework-2.6.x)](https://github.com/anshulbajpai/scala-playframework-effects/actions?query=workflow%3Abuild+branch%3Aplayframework-2.6.x)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.anshulbajpai/scala-playframework-effects_2.12/2.6.1.1?label=maven)](https://search.maven.org/artifact/com.github.anshulbajpai/scala-playframework-effects_2.12/2.6.1.1/jar)


## Underlying needs

There were three major underlying reasons which led to the creation of this library -

- ### Error handling

Often working on projects that use playframework, I observed that teams used failed `Future` to model business errors.
The recipe is simple -
1. Create an exception case class for a business error
2. Wrap the exception in a failed `Future` and return it from the service which reaches the action block and is returned from there.
3. Handle that exception in the playframework's global error handler to convert it into a `Result`.

That's it. This seems to be such an easy way of error handling that many of us might have done it this way at least once in the past, if not with the playframework but with some other library.

There are few downsides to this approach though 

- The business error encoding is lost in `Future`'s syntax. Looking at a `Future` returned from a service, we can't tell which business error it could be carrying.
   It is also not obvious how to handle a business error in a service thrown by another collaborator service. 
   We will have to peek into the code of collaborator service code to find out which exception to handle.

- The controller action is not complete in its own. The action block does not return a `Result` but instead a failed `Future` in case of business error.
   The mapping of failed `Future` to `Result` is done in a global error handler. 
   This also makes unit testing of the controller action incomplete. We can only test that the action will return a failed `Future` when a business error occurs.
   We will have to write a separate test for global error handler which can't give any confidence that the business error will be translated into correct `Result` as the action method and the global error handler are disconnected.
   To get full confidence, we will have to write an expensive integration test.   

A better approach could be to return `Future[Either[E, A]]` from the services which force the action block to handle
 the error and convert the `Either` into `Result` for both success and error.

If we follow this approach of returning `Future[Either[E, A]]` from the services, we will realise that our action
 methods will become heavy as they will now have to handle the `Either` for both success and error. 
This seems to be a problem of its own. Either we will handle the `Either` in the controller or create some helper method
 outside the controller to handle the `Either`.   
Handling it in some helper method controller is not that bad, but we still need to call that method in the action method.

This library tries to solve the problem of handling the `Either` in a neater approach.
 
- ### Sensible defaults

Often while writing action methods for RESTful services, we have to convert a case class into an `Ok` result returning a
 JSON response or we have to convert a `Future.successful(())` into a `NoContent` response.  

This library provides some sensible defaults to allow easier conversion to a `Result` 

- ### Bring your own effect

We can create services where the effect is abstracted, but the controller's action methods are always tied to the only `Future` effect. Whatever
 the effect we use for our services, 
 e.g. [IO](https://typelevel.org/cats-effect/datatypes/io.html), 
 [Task](https://monix.io/docs/2x/eval/task.html), 
 [ZIO](https://zio.dev/), we still have to convert it into a `Future`.  

This library provides a way to create action methods which are abstracted from effect. This allows writing controllers using the tagless-final approach as well.


## Usage

Add `scala-playframework-effects` as SBT dependency

```sbt
libraryDependencies += "com.github.anshulbajpai" %% "scala-playframework-effects" % "(version)"
```

The minimum playframework version we support is 2.6.x. Our versioning strategy is aligned with playframework's versions up to their minor version, e.g. - `2.7.1.0` library version will work with play 2.7.x. The `1.0` sub-version
represents the version of this library for play 2.7.x.   


Actions can be created using `Action.asyncF` and `Action.sync` methods after importing `com.github.anshulbajpai.scala_play_effect.ActionBuilderOps._`.


Assuming the following code is present in the scope.

```scala
import com.github.anshulbajpai.scala_play_effect.ActionBuilderOps._
import cats.~>

implicit val request = FakeRequest().withJsonBody(Json.obj("message" -> "some message"))

case class ActionMessage(message: String)
implicit val messageWrites: Writes[ActionMessage] = Json.writes[ActionMessage]

case class ActionError(error: String)
implicit val errorWrites: Writes[ActionError] = Json.writes[ActionError]

implicit val actionErrorToResult: ToResult[ActionError] = new ToResult[ActionError] {
    override def toResult(error: ActionError): Result = Results.BadRequest(Json.toJson(error))
}

implicit val ioToFuture: IO ~> Future = λ[IO ~> Future](_.unsafeToFuture())
/*
  The above `ioToFuture` implicit can also be written as below. We have used kind-projector compiler plugin for brevity above.
  implicit val ioToFuture: FunctionK[IO, Future] =  new FunctionK[IO, Future] {
    override def apply[A](fa: IO[A]): Future[A] = fa.unsafeToFuture()
  }
*/
```

### asyncF
The `asyncF` method helps create Actions from blocks which can return a value wrapped in an effect `F[_]`.
It also comes with some sensible defaults to map the block's return type to a proper `Result`. For example an action block
returning a Future[Unit] will be converted into an HTTP NoContent status code.  

- Returning `Future[Unit]`

```scala
val action = Action.asyncF { _ =>
    Future.unit
}
// action: play.api.mvc.Action[play.api.mvc.AnyContent] = Action(parser=BodyParser((no name)))
val result = call(action, request) // call is imported from play.api.test.Helpers
// result: Future[Result] = Future(Success(Result(204, TreeMap()))) // call is imported from play.api.test.Helpers
status(result)
// res0: Int = 204
```

- Returning `Future[A]`

```scala
val action = Action.asyncF { _ =>
    Future.successful(ActionMessage("some message"))
}
// action: play.api.mvc.Action[play.api.mvc.AnyContent] = Action(parser=BodyParser((no name)))
val result = call(action, request)
// result: Future[Result] = Future(Success(Result(200, TreeMap())))
status(result)
// res1: Int = 200
contentAsJson(result)
// res2: play.api.libs.json.JsValue = JsObject(
//   Map("message" -> JsString("some message"))
// )
```

- Returning `Future[Either[A, B]]`

```scala
val successAction = Action.asyncF { _ =>
    Future.successful(ActionMessage("some message").asRight[ActionError])
}
// successAction: play.api.mvc.Action[play.api.mvc.AnyContent] = Action(parser=BodyParser((no name)))
val successActionResult = call(successAction, request)
// successActionResult: Future[Result] = Future(Success(Result(200, TreeMap())))
status(successActionResult)
// res3: Int = 200
contentAsJson(successActionResult)
// res4: play.api.libs.json.JsValue = JsObject(
//   Map("message" -> JsString("some message"))
// )

val errorAction = Action.asyncF { _ =>
    Future.successful(ActionError("some error").asLeft[ActionMessage])
}
// errorAction: play.api.mvc.Action[play.api.mvc.AnyContent] = Action(parser=BodyParser((no name)))
val errorActionResult = call(errorAction, request)
// errorActionResult: Future[Result] = Future(Success(Result(400, TreeMap())))
status(errorActionResult)
// res5: Int = 400
contentAsJson(errorActionResult)
// res6: play.api.libs.json.JsValue = JsObject(
//   Map("error" -> JsString("some error"))
// )
```

- Using the request body 

```scala
val successAction = Action(json).asyncF { req =>
    Future.successful(ActionMessage((req.body \ "message").as[String]).asRight[ActionError])
}
// successAction: play.api.mvc.Action[play.api.libs.json.JsValue] = Action(parser=BodyParser(conditional, wrapping=BodyParser(json, maxLength=102400)))
val successActionResult = call(successAction, request)
// successActionResult: Future[Result] = Future(Success(Result(200, TreeMap())))
status(successActionResult)
// res7: Int = 200
contentAsJson(successActionResult)
// res8: play.api.libs.json.JsValue = JsObject(
//   Map("message" -> JsString("some message"))
// )
```


- Returning `IO[Unit]`

```scala
val action = Action.asyncF { _ =>
    IO.unit
}
// action: play.api.mvc.Action[play.api.mvc.AnyContent] = Action(parser=BodyParser((no name)))
val result = call(action, request)
// result: Future[Result] = Future(Success(Result(204, TreeMap())))
status(result)
// res9: Int = 204
```

- Returning `Result` as it is

```scala
val action = Action.asyncF { _ =>
    IO.pure(Results.NoContent)
}
// action: play.api.mvc.Action[play.api.mvc.AnyContent] = Action(parser=BodyParser((no name)))
val result = call(action, request)
// result: Future[Result] = Future(Success(Result(204, TreeMap())))
status(result)
// res10: Int = 204
```

### sync
The `sync` method is similar to `asyncF` in all aspects except that it doesn't need an effect to be returned in the return type of action block.
It also comes with the same sensible defaults as `asyncF`

```scala
val action = Action.sync { _ =>
    ActionMessage("some message")
}
// action: play.api.mvc.Action[play.api.mvc.AnyContent] = Action(parser=BodyParser((no name)))
val result = call(action, request)
// result: Future[Result] = Future(Success(Result(200, TreeMap())))
status(result)
// res11: Int = 200
contentAsJson(result)
// res12: play.api.libs.json.JsValue = JsObject(
//   Map("message" -> JsString("some message"))
// )
```

These examples and more cases are covered in [AsyncActionSpecs.scala](core/src/test/scala/com/github/anshulbajpai/scala_play_effect/AsyncActionSpecs.scala)
 and [SyncActionSpecs.scala](core/src/test/scala/com/github/anshulbajpai/scala_play_effect/SyncActionSpecs.scala)

## How does it work

We take help of [cats](https://typelevel.org/cats/) natural transformation data type [FunctionK](https://typelevel.org/cats/datatypes/functionk.html) and our own
`ToResult` typeclass to do all under-the-hood transformation. 

```scala
trait ToResult[S] { 
  def toResult(s: S): Result
}
```

If your code has a `FunctionK[F, Future]` instance available for an effect `F[_]` (needs to be a `Functor` too) and a `ToResult` instance for a type `S`, 
then you can create an action like this

```scala
Action.asyncF { req =>
  // return F[S]
}
```

## Using tagless-final

Using this library, it is very easy to create tagless-final components right up to the controllers.
There is an example play application included in the repository under the [exampleApp](exampleApp) directory which shows
how to use tagless-final end-to-end using this library and [macwire](https://github.com/softwaremill/macwire). 
The [HelloController.scala](exampleApp/app/controllers/HelloController.scala) has examples on how to use this library to write actions method effortlessly without thinking of HTTP status code and serialization most of the time.

## mdoc
This README is generated via [mdoc](https://scalameta.org/mdoc/). This is the [source file](docs/README.md)
