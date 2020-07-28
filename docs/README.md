# scala-playframework-effects

Scala [playframework](https://github.com/playframework/playframework) Action builders on batteries.

[![License](https://img.shields.io/hexpm/l/apa?style=plastic)](https://github.com/anshulbajpai/scala-playframework-effects/blob/master/LICENSE)

## Versions

### Play 2.8.x

[![Build Status](https://github.com/anshulbajpai/scala-playframework-effects/workflows/build/badge.svg?branch=playframework-2.8.x)](https://github.com/anshulbajpai/scala-playframework-effects/actions?query=workflow%3Abuild+branch%3Aplayframework-2.8.x)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.anshulbajpai/scala-playframework-effects_2.13/2.8.1.0?label=maven)](https://search.maven.org/artifact/com.github.anshulbajpai/scala-playframework-effects_2.13/2.8.1.0/jar)

### Play 2.7.x

[![Build Status](https://github.com/anshulbajpai/scala-playframework-effects/workflows/build/badge.svg?branch=playframework-2.7.x)](https://github.com/anshulbajpai/scala-playframework-effects/actions?query=workflow%3Abuild+branch%3Aplayframework-2.7.x)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.anshulbajpai/scala-playframework-effects_2.13/2.7.1.0?label=maven)](https://search.maven.org/artifact/com.github.anshulbajpai/scala-playframework-effects_2.13/2.7.1.0/jar)

### Play 2.6.x

[![Build Status](https://github.com/anshulbajpai/scala-playframework-effects/workflows/build/badge.svg?branch=playframework-2.6.x)](https://github.com/anshulbajpai/scala-playframework-effects/actions?query=workflow%3Abuild+branch%3Aplayframework-2.6.x)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.anshulbajpai/scala-playframework-effects_2.12/2.6.1.0?label=maven)](https://search.maven.org/artifact/com.github.anshulbajpai/scala-playframework-effects_2.12/2.6.1.0/jar)


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

- ### Using alternative to Future effect

We create services where the effect is abstracted, but the controller's action methods are locked to `Future`. Whatever
 the effect we use for our services, 
 e.g. [IO](https://typelevel.org/cats-effect/datatypes/io.html), 
 [Task](https://monix.io/docs/2x/eval/task.html), 
 [ZIO](https://zio.dev/), we still have to convert it into a `Future`.  

This library provides a way to create action methods which are abstracted from effect. At the moment, we only support
 [IO](https://typelevel.org/cats-effect/datatypes/io.html). This allows writing controllers using the tagless-final approach as well.


## Usage

Add `scala-playframework-effects` as SBT dependency

```sbt
libraryDependencies += "com.github.anshulbajpai" %% "scala-playframework-effects" % "(version)"
```

The minimum playframework version we support is 2.6.x. Our versioning strategy is aligned with playframework's versions up to their minor version, e.g. - `2.7.1.0` library version will work with play 2.7.x. The `1.0` sub-version
represents the version of this library for play 2.7.x.   


Actions can be created using `Action.asyncF` and `Action.sync` methods after importing `com.github.anshulbajpai.scala_play_effect.ActionBuilderOps._`.

```scala mdoc:invisible
import play.api.mvc.DefaultActionBuilder
import play.api.mvc.PlayBodyParsers
import play.api.mvc.Result
import play.api.mvc.Results
import akka.actor.ActorSystem
import akka.stream.Materializer._
import scala.concurrent.Future
import play.api.test.Helpers._
import play.api.test.FakeRequest
import play.api.libs.json.{ Json, Writes }
import com.github.anshulbajpai.scala_play_effect.ToResult
import cats.instances.future._
import cats.syntax.either._
import cats.effect.IO

implicit val system = ActorSystem()
implicit val ec = system.dispatcher
val bodyParsers = PlayBodyParsers()
import bodyParsers.json

val Action = DefaultActionBuilder(bodyParsers.default)
```

Assuming the following code is present in the scope.

```scala mdoc:silent
import com.github.anshulbajpai.scala_play_effect.ActionBuilderOps._

implicit val request = FakeRequest().withJsonBody(Json.obj("message" -> "some message"))

case class ActionMessage(message: String)
implicit val messageWrites: Writes[ActionMessage] = Json.writes[ActionMessage]

case class ActionError(error: String)
implicit val errorWrites: Writes[ActionError] = Json.writes[ActionError]

implicit val actionErrorToResult: ToResult[ActionError] = new ToResult[ActionError] {
    override def toResult(error: ActionError): Result = Results.BadRequest(Json.toJson(error))
}

```

### asyncF
The `asyncF` method helps create Actions from blocks which can return a value wrapped in an effect `F[_]`.
It also comes with some sensible defaults to map the block's return type to a proper `Result`. For example an action block
returning a Future[Unit] will be converted into an HTTP NoContent status code.  

- Returning `Future[Unit]`

```scala mdoc:nest
val action = Action.asyncF { _ =>
    Future.unit
}
val result = call(action, request) // call is imported from play.api.test.Helpers
status(result)
```

- Returning `Future[A]`

```scala mdoc:nest
val action = Action.asyncF { _ =>
    Future.successful(ActionMessage("some message"))
}
val result = call(action, request)
status(result)
contentAsJson(result)
```

- Returning `Future[Either[A, B]]`

```scala mdoc:nest
val successAction = Action.asyncF { _ =>
    Future.successful(ActionMessage("some message").asRight[ActionError])
}
val successActionResult = call(successAction, request)
status(successActionResult)
contentAsJson(successActionResult)

val errorAction = Action.asyncF { _ =>
    Future.successful(ActionError("some error").asLeft[ActionMessage])
}
val errorActionResult = call(errorAction, request)
status(errorActionResult)
contentAsJson(errorActionResult)
```

- Using the request body 

```scala mdoc:nest
val successAction = Action(json).asyncF { req =>
    Future.successful(ActionMessage((req.body \ "message").as[String]).asRight[ActionError])
}
val successActionResult = call(successAction, request)
status(successActionResult)
contentAsJson(successActionResult)
```


- Returning `IO[Unit]`

```scala mdoc:nest
val action = Action.asyncF { _ =>
    IO.unit
}
val result = call(action, request)
status(result)
```

- Returning `Result` as it is

```scala mdoc:nest
val action = Action.asyncF { _ =>
    IO.pure(Results.NoContent)
}
val result = call(action, request)
status(result)
```

### sync
The `sync` method is similar to `asyncF` in all aspects except that it doesn't need an effect to be returned in the return type of action block.
It also comes with the same sensible defaults as `asyncF`

```scala mdoc:nest
val action = Action.sync { _ =>
    ActionMessage("some message")
}
val result = call(action, request)
status(result)
contentAsJson(result)
```

These examples and more cases are covered in `AsyncActionSpecs.scala` and `SyncActionSpecs.scala`

## Using tagless-final

There is an example play application included in the repository under the `exampleApp` directory.
The application is using the tagless-final approach end-to-end. The DI is done via [macwire](https://github.com/softwaremill/macwire)
The `HelloController.scala` has examples on how to use this library to write actions method effortlessly without thinking of HTTP status code and serialization most of the time.


## mdoc
This README is generated via [mdoc](https://scalameta.org/mdoc/). The is the [source file](README.md)