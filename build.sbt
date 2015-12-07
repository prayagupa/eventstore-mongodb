name := "EventStore-MongoDB"

organization := "EventStore"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.11.2"

crossScalaVersions := Seq("2.10.4", "2.11.2")

libraryDependencies ++= Seq(
  "org.mongodb" % "mongo-java-driver" % "3.1.0",
  "org.reactivemongo" %% "reactivemongo" % "0.11.7",
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "org.scalacheck" %% "scalacheck" % "1.11.5" % "test"
)

initialCommands := "import EventStore._"
