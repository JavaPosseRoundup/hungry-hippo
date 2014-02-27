import play.Project._

name := """hungry-hippo"""

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  "org.webjars" %% "webjars-play" % "2.2.0", 
  "org.webjars" % "bootstrap" % "2.3.1",
  "org.scalatest" %% "scalatest" % "2.0" % "test",
  "org.reactivemongo" %% "play2-reactivemongo" % "0.10.2",
  "com.typesafe.akka" % "akka-testkit_2.10" % "2.2.3" % "test"
)

playScalaSettings
