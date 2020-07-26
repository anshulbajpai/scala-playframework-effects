import sbt.url

ThisBuild / organizationName := "com.github.anshulbajpai"
ThisBuild / organizationHomepage := Some(url("https://github.com/anshulbajpai"))

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/anshulbajpai/scala-playframework-effects"),
    "scm:git@github.com:anshulbajpai/scala-playframework-effects.git"
  )
)

ThisBuild / developers := List(
  Developer(
    id    = "anshulbajpai",
    name  = "Anshul Bajpai",
    email = "bajpai.anshul@gmail.com",
    url   = url("https://www.linkedin.com/in/anshulbajpai/")
  )
)

ThisBuild / description := "Add effects support for Scala playframework"
ThisBuild / licenses := List("Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt"))
ThisBuild / homepage := Some(url("https://github.com/anshulbajpai/scala-playframework-effects"))

// Remove all additional repository other than Maven Central from POM
ThisBuild / pomIncludeRepository := { _ => false }
ThisBuild / publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}
ThisBuild / publishMavenStyle := true
