import play.sbt.routes.RoutesKeys
import Dependencies._
import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._
import sbtrelease.ReleasePlugin.autoImport.releaseProcess
import sbtrelease.ReleaseStateTransformations.runClean

ThisBuild / organization      := "com.github.anshulbajpai"
ThisBuild / scalafmtOnCompile := true
ThisBuild / publishTo         := sonatypePublishToBundle.value
ThisBuild / scalaVersion      := "2.12.12"

lazy val root = (project in file("."))
  .settings(
    publish / skip     := true
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
  scalacOptions ++= Seq("-P:wartremover:traverser:org.wartremover.warts.Unsafe"),
  libraryDependencies ++= Seq(
    play,
    catsEffect,
    simulacrum,
    scalaTestPlay,
    compilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)
  )
)

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  releaseStepCommand("publishSigned"),
  releaseStepCommand("sonatypeBundleRelease"),
  setNextVersion,
  commitNextVersion,
  pushChanges
)
