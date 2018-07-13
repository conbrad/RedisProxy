name := "RedisProxy"
organization := "conbrad"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.6"

libraryDependencies += guice
libraryDependencies += "com.github.etaty" %% "rediscala" % "1.8.0"
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test

// Prevents PID clashes
javaOptions in Universal ++= Seq(
  "-Dpidfile.path=/dev/null"
)
