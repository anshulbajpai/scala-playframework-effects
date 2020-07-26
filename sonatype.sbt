import sbt.url

ThisBuild / sonatypeProfileName := "com.github.anshulbajpai"

ThisBuild / publishMavenStyle := true

ThisBuild / licenses := Seq("Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt"))

ThisBuild /  homepage := Some(url("https://github.com/anshulbajpai/scala-playframework-effects"))
ThisBuild /  scmInfo := Some(
  ScmInfo(
    url("https://github.com/anshulbajpai/scala-playframework-effects"),
    "scm:git@github.com:anshulbajpai/scala-playframework-effects.git"
  )
)

ThisBuild / developers := List(
  Developer(id="anshulbajpai", name="Anshul Bajpai", email="bajpai.anshul@gmail.com", url=url("https://www.linkedin.com/in/anshulbajpai"))
)