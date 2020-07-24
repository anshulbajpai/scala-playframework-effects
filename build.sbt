import play.sbt.routes.RoutesKeys
import Dependencies._

lazy val root = (project in file(".")).settings(
  name := "scala-playframework-tools",
  organization := "com.github.anshulbajpai",
  version := "1.0-SNAPSHOT",
  commonSettings,
).aggregate(exampleApp)

lazy val exampleApp = (project in file("exampleApp")).enablePlugins(PlayScala).settings(
  commonSettings,
  RoutesKeys.routesImport -= "controllers.Assets.Asset",
  libraryDependencies ++= Seq(
    macwire
  )
).dependsOn(core)

lazy val core = (project in file("core")).settings(
  commonSettings,
  scalacOptions += "-Ymacro-annotations",
  libraryDependencies ++= Seq(
    play,
    catsEffect,
    simulacrum
  ),
  scalacOptions += "-P:wartremover:traverser:org.wartremover.warts.Unsafe"
)

lazy val commonSettings = Seq(
  scalaVersion := "2.13.3",
  scalafmtOnCompile := true
)