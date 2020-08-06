import play.sbt.routes.RoutesKeys
import Dependencies._
import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._
import sbtrelease.ReleasePlugin.autoImport.releaseProcess
import sbtrelease.ReleaseStateTransformations.runClean

lazy val scala212               = "2.12.12"
lazy val scala213               = "2.13.3"
lazy val supportedScalaVersions = List(scala212, scala213)

ThisBuild / organization       := "com.github.anshulbajpai"
ThisBuild / crossScalaVersions := supportedScalaVersions
ThisBuild / scalafmtOnCompile  := true
ThisBuild / publishTo          := sonatypePublishToBundle.value
ThisBuild / scalaVersion       := scala213

lazy val root = (project in file("."))
  .settings(
    publish / skip     := true,
    crossScalaVersions := Nil
  )
  .aggregate(core, exampleApp)

lazy val exampleApp = (project in file("exampleApp"))
  .enablePlugins(PlayScala)
  .settings(
    publish / skip := true,
    RoutesKeys.routesImport -= "controllers.Assets.Asset",
    addCompilerPlugin(kindProjector),
    libraryDependencies ++= Seq(
      catsEffect,
      macwire
    )
  )
  .dependsOn(core)

lazy val core = (project in file("core")).settings(
  name := "scala-playframework-effects",
  scalacOptions ++= Seq("-P:wartremover:traverser:org.wartremover.warts.Unsafe") ++ scalacOptionsVersion(
    scalaVersion.value
  ),
  addCompilerPlugin(kindProjector),
  libraryDependencies ++= Seq(
    play,
    cats,
    simulacrum,
    scalaTestPlay,
    catsEffect % Test
  ) ++ scalaVersionBasedDependencies(scalaVersion.value)
)

lazy val docs = project
  .in(file("core-docs"))
  .dependsOn(core)
  .settings(
    addCompilerPlugin(kindProjector),
    mdocOut            := baseDirectory.value.getParentFile,
    mdocExtraArguments := Seq("--no-link-hygiene"),
    libraryDependencies ++= Seq(
      scalaTestPlay.withConfigurations(Some(Compile.name)),
      catsEffect
    )
  )
  .enablePlugins(MdocPlugin)

def scalaVersionBasedDependencies(version: String) = {
  if (version.startsWith("2.13")) Seq.empty
  else
    Seq(compilerPlugin(macroParadise))
}

def scalacOptionsVersion(version: String) = {
  if (version.startsWith("2.13")) Seq("-Ymacro-annotations") else Seq.empty
}

releaseCrossBuild := false
releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  releaseStepCommandAndRemaining("+test"),
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  releaseStepCommandAndRemaining("+publishSigned"),
  releaseStepCommand("sonatypeBundleRelease"),
  setNextVersion,
  commitNextVersion,
  pushChanges
)
