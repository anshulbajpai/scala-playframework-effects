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
    addCompilerPlugin(kindProjector),
    libraryDependencies ++= Seq(
      catsEffect,
      macwire
    )
  )
  .dependsOn(core)

lazy val core = (project in file("core")).settings(
  name := "scala-playframework-effects",
  scalacOptions ++= Seq("-P:wartremover:traverser:org.wartremover.warts.Unsafe"),
  addCompilerPlugin(kindProjector),
  libraryDependencies ++= Seq(
    play,
    cats,
    simulacrum,
    scalaTestPlay,
    catsEffect % Test,
    compilerPlugin(macroParadise)
  )
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
