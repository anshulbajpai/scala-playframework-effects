import sbt._

object Dependencies {

  object Versions {
    val playVersion          = "2.8.2"
    val catsEffectVersion    = "2.1.4"
    val catsVersion          = "2.0.0"
    val simulacrumVersion    = "1.0.0"
    val scalaTestPlayVersion = "5.1.0"
    val macwireVersion       = "2.3.6"
    val macroParadiseVersion = "2.1.1"
    val kindProjectorVersion = "0.11.0"
  }

  import Versions._

  val play          = "com.typesafe.play"        %% "play"               % playVersion
  val cats          = "org.typelevel"            %% "cats-core"          % catsVersion
  val catsEffect    = "org.typelevel"            %% "cats-effect"        % catsEffectVersion
  val simulacrum    = "org.typelevel"            %% "simulacrum"         % simulacrumVersion
  val macwire       = "com.softwaremill.macwire" %% "macros"             % macwireVersion % "provided"
  val scalaTestPlay = "org.scalatestplus.play"   %% "scalatestplus-play" % scalaTestPlayVersion % Test
  val macroParadise = "org.scalamacros"          % "paradise"            % macroParadiseVersion cross CrossVersion.full
  val kindProjector = "org.typelevel"            % "kind-projector"      % kindProjectorVersion cross CrossVersion.full
}
