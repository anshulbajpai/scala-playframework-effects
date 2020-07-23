import sbt._

object Dependencies {

  object Versions {
    val playVersion = "2.8.2"
    val catsEffectVersion = "2.1.4"
    val simulacrumVersion = "1.0.0"
  }

  import Versions._

  val play = "com.typesafe.play" %% "play" % playVersion
  val catsEffect = "org.typelevel" %% "cats-effect" % catsEffectVersion
  val simulacrum = "org.typelevel" %% "simulacrum" % simulacrumVersion

}
