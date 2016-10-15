name := "EventStoreMongoDB"

organization := "EventStore"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.11.2"

crossScalaVersions := Seq("2.10.4", "2.11.2")

//resolvers += "mvnrepository" at "http://mvnrepository.com/artifact/"

libraryDependencies ++= Seq(
  "org.json" % "json" % "20151123",
  //"net.sf.json-lib" % "json-lib" % "2.3",
//  "com.eclipsesource.minimal-json" % "minimal-json" % "0.9.4",
  "org.mongodb" % "mongo-java-driver" % "3.1.0",
  "org.reactivemongo" %% "reactivemongo" % "0.11.7",
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "org.scalacheck" %% "scalacheck" % "1.11.5" % "test"
)

unmanagedBase := baseDirectory.value / "custom_lib"

initialCommands := "import eventStreaming._"
