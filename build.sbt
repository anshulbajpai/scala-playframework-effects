name := """scala-playframework-tools"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.3"

libraryDependencies += guice
libraryDependencies += "org.typelevel" %% "cats-effect" % "2.1.4"
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test
libraryDependencies += "org.typelevel" %% "simulacrum" % "1.0.0"

scalacOptions += "-Ymacro-annotations"
