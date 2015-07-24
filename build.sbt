import sbtrelease.ReleasePlugin._

name := "schale"

organization := "pl.newicom"

version := "1.0.1"

scalaVersion := "2.11.7"

licenses := ("Apache2", new java.net.URL("http://github.com/pawelkaczor/schale/blob/master/LICENSE")) :: Nil

homepage := Some(new java.net.URL("http://github.com/pawelkaczor/schale"))

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

publishMavenStyle := true

libraryDependencies ++= Seq(
	"com.typesafe.akka" %% "akka-actor" % "2.4-M1",
	"org.scalatest" %% "scalatest" % "2.2.4" % "test"
)


Publish.settings ++ releaseSettings