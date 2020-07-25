import play.sbt.routes.RoutesKeys
import Dependencies._

lazy val scala212               = "2.12.12"
lazy val scala213               = "2.13.3"
lazy val supportedScalaVersions = List(scala212, scala213)

lazy val root = (project in file("."))
  .settings(
    name         := "scala-playframework-effects",
    organization := "com.github.anshulbajpai",
    version      := "1.0-SNAPSHOT",
    commonSettings,
    crossScalaVersions := Nil
  )
  .aggregate(core, exampleApp)

lazy val exampleApp = (project in file("exampleApp"))
  .enablePlugins(PlayScala)
  .settings(
    commonSettings,
    RoutesKeys.routesImport -= "controllers.Assets.Asset",
    libraryDependencies ++= Seq(
      macwire
    )
  )
  .dependsOn(core)

lazy val core = (project in file("core")).settings(
  commonSettings,
  scalacOptions ++= Seq("-P:wartremover:traverser:org.wartremover.warts.Unsafe") ++ scalacOptionsVersion(scalaVersion.value),
  libraryDependencies ++= Seq(
    play,
    catsEffect,
    simulacrum,
    scalaTestPlay
  ) ++ scalaVersionBasedDependencies(scalaVersion.value)
)

def scalaVersionBasedDependencies(version: String) = {
  if (version.startsWith("2.13")) Seq.empty else
  Seq(compilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full))
}

lazy val commonSettings = Seq(
  crossScalaVersions := supportedScalaVersions,
  scalafmtOnCompile  := true
)

def scalacOptionsVersion(version: String) = {
  if (version.startsWith("2.13")) Seq("-Ymacro-annotations") else Seq.empty
}
