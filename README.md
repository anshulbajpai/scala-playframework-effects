# scala-playframework-effects

This is  a library that add effects support to Scala [playframework](https://github.com/playframework/playframework) Actions.

[![Build Status](https://github.com/anshulbajpai/scala-playframework-effects/workflows/build/badge.svg?branch=master)](https://github.com/anshulbajpai/scala-playframework-effects/actions?query=workflow%3Abuild+branch%3Amaster)
[![License](https://img.shields.io/hexpm/l/apa?style=plastic)](https://github.com/anshulbajpai/scala-playframework-effects/blob/master/LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.anshulbajpai/scala-playframework-effects_2.13/0.1.0?style=plastic)](https://search.maven.org/artifact/com.github.anshulbajpai/scala-playframework-effects_2.13/0.1.0/jar)

## Underlying needs

There were three major underlying reasons which led to the creation of this library -

- ### Error handling

Often working on projects that use playframework, I observed that teams used failed `Future` to represent business errors.
The recipe is simple -
1. Create an exception case class for a business error
2. Wrap the exception in a failed `Future` and return it from the service which reaches the `Action` block and is returned from there.
3. Handle that exception in the playframework's global error handler to convert it into a `Result`.

That's it. This seems to be such an easy way of error handling that many of us might have done it this way atleast once in the past. If not with the playframework but with some other library.

There are few downsides to this approach though 

- The business error encoding is lost in `Future`'s syntax. Looking at a Future returned from a service, we can't tell which business error it could be carrying.
   It is also not obvious how to handle a business in error in a service thrown by another collaborator service. 
   We will have to peek into the code of collaborator service code to find out which exception to handle.

- The controller action is not complete in its own. The action method is part complete as it is not returning a `Result` but instead a failed `Future` in case of business error.
   The mapping of failed `Future` to `Result` is done in global error handler. 
   This also makes unit testing of action method incomplete. We can only test that the action method will return a failed `Future` when a business error occurs.
   We will have to write a separate test for global error handler which can't give any confidence that the business error will be translated into correct `Result` as the action method and the global error handler are disconnected.
   To get the full confidence, we will have to write a expensive integration test.   

A better approach could be to return `Future[Either[E, A]]` from the services which forces the action method to handle the error and convert the `Either` into `Result`

If we follow this approach of returning `Future[Either[E, A]]` from the services, we will realise that our action methods will become heavy as they will now have to handle the `Either`.
This seems to be a problem of its own. Either we will handle the `Either` in the controller or create some helper method outside controller to handle the `Either`.   
Handling it in some helper method controller is not that bad, but we still need to call that method in the action method.

This library tries to solve the problem of handling the `Either` in a neater approach.
 
- ### Sensible defaults

Often when writing action methods for RESTful services, we have to convert a case class into a `Ok` result returning a JSON response or we have to convert a `Future.successful(())` into a `NoContent` response.  

This library provides some sensible defaults to allow easier conversion to a `Result` 

- ### Using alternative to Future effect

We create services where the effect is abstracted, but the controller's action method are locked to `Future`. Whatever effect we use for our services, e.g. [IO](https://typelevel.org/cats-effect/datatypes/io.html), [Task](https://monix.io/docs/2x/eval/task.html), [ZIO](https://zio.dev/), we still have to conver it into a `Future`.  

This library provides a way to create action methods which are abstracted from effect. At the moment, we only support [IO](https://typelevel.org/cats-effect/datatypes/io.html). 