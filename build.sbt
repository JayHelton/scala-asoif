scalaVersion := "2.13.1"

name := "ASOIF-Scala-Client"
organization := "orbits.consult.com"
version := "1.0"

libraryDependencies += Seq(
  "com.typesafe.akka" %% "akka-stream" % "2.5.26",
  "com.typesafe.akka" %% "akka-http" % "10.1.11",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.11"
)

