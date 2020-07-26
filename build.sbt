import play.sbt.routes.RoutesKeys
import Dependencies._

lazy val scala212               = "2.12.12"
lazy val scala213               = "2.13.3"
lazy val supportedScalaVersions = List(scala212, scala213)

ThisBuild / organization := "com.github.anshulbajpai"
ThisBuild / crossScalaVersions := supportedScalaVersions
ThisBuild / scalafmtOnCompile := true

lazy val root = (project in file("."))
  .settings(
    publish / skip := true,
    crossScalaVersions := Nil
  )
  .aggregate(core, exampleApp)

lazy val exampleApp = (project in file("exampleApp"))
  .enablePlugins(PlayScala)
  .settings(
    publish / skip := true,
    RoutesKeys.routesImport -= "controllers.Assets.Asset",
    libraryDependencies ++= Seq(
      macwire
    )
  )
  .dependsOn(core)

lazy val core = (project in file("core")).settings(
  name := "scala-playframework-effects",
  version      := "0.1.0-SNAPSHOT",
  scalacOptions ++= Seq("-P:wartremover:traverser:org.wartremover.warts.Unsafe") ++ scalacOptionsVersion(
    scalaVersion.value
  ),
  libraryDependencies ++= Seq(
    play,
    catsEffect,
    simulacrum,
    scalaTestPlay
  ) ++ scalaVersionBasedDependencies(scalaVersion.value)
)

def scalaVersionBasedDependencies(version: String) = {
  if (version.startsWith("2.13")) Seq.empty
  else
    Seq(compilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full))
}

def scalacOptionsVersion(version: String) = {
  if (version.startsWith("2.13")) Seq("-Ymacro-annotations") else Seq.empty
}
