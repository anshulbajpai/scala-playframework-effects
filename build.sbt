import play.sbt.routes.RoutesKeys

lazy val root = (project in file(".")).settings(
  name := "scala-playframework-tools",
  organization := "com.github.anshulbajpai",
  version := "1.0-SNAPSHOT",
  commonSettings,
).aggregate(exampleApp)

lazy val exampleApp = (project in file("exampleApp")).enablePlugins(PlayScala).settings(
  commonSettings,
  libraryDependencies ++= Seq(
    guice,
  ),
  RoutesKeys.routesImport -= "controllers.Assets.Asset",
).dependsOn(core)

lazy val core = (project in file("core")).settings(
  commonSettings,
  scalacOptions += "-Ymacro-annotations",
  libraryDependencies ++= Seq(
    "com.typesafe.play" %% "play" % "2.8.2",
    "org.typelevel" %% "cats-effect" % "2.1.4",
    "org.typelevel" %% "simulacrum" % "1.0.0",
    "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test,
  ),
  scalacOptions += "-P:wartremover:traverser:org.wartremover.warts.Unsafe"
)

lazy val commonSettings = Seq(
  scalaVersion := "2.13.3",
  scalafmtOnCompile := true
)