lazy val root = (project in file(".")).settings(
  name := "scala-playframework-tools",
  organization := "com.github.anshulbajpai",
  version := "1.0-SNAPSHOT",
  commonSettings,
).aggregate(exampleApp)

lazy val exampleApp = (project in file("exampleApp")).enablePlugins(PlayScala).settings(
  commonSettings,
  scalacOptions += "-Ymacro-annotations",
  libraryDependencies ++= Seq(
    guice,
    "org.typelevel" %% "cats-effect" % "2.1.4",
    "org.typelevel" %% "simulacrum" % "1.0.0",
    "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test
  )
)

lazy val commonSettings = Seq(
  scalaVersion := "2.13.3",
)


