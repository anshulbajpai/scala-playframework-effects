import sbt._

object Dependencies {

  object Versions {
    val playVersion = "2.7.5"
    val catsEffectVersion = "2.1.4"
    val simulacrumVersion = "1.0.0"
    val scalaTestPlayVersion = "4.0.3"
    val macwireVersion = "2.3.6"
    val macroParadiseVersion = "2.1.1"
  }

  import Versions._

  val play = "com.typesafe.play" %% "play" % playVersion
  val catsEffect = "org.typelevel" %% "cats-effect" % catsEffectVersion
  val simulacrum = "org.typelevel" %% "simulacrum" % simulacrumVersion
  val macwire = "com.softwaremill.macwire" %% "macros" % macwireVersion % "provided"
  val scalaTestPlay = "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlayVersion % "test"
  val macroParadise = "org.scalamacros"          % "paradise"            % macroParadiseVersion cross CrossVersion.full
}
