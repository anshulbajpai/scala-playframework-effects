import sbt.url

ThisBuild / organizationHomepage := Some(url("https://github.com/anshulbajpai"))
ThisBuild / description := "Add effects support for Scala playframework"

ThisBuild / sonatypeProfileName := "com.github.anshulbajpai"

ThisBuild / publishMavenStyle := true

ThisBuild / licenses := Seq("Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt"))

import xerial.sbt.Sonatype._
ThisBuild / sonatypeProjectHosting := Some(GitHubHosting("anshulbajpai", "scala-playframework-effects", "bajpai.anshul@gmail.com"))
ThisBuild / developers := List(
  Developer(id="anshulbajpai", name="Anshul Bajpai", email="bajpai.anshul@gmail.com", url=url("https://www.linkedin.com/in/anshulbajpai"))
)