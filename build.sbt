import sbtrelease.ReleasePlugin._

name := "schale"

organization := "pl.newicom"

version := "1.0.2"

scalaVersion := "2.12.2"

licenses := ("Apache2", new java.net.URL("http://github.com/pawelkaczor/schale/blob/master/LICENSE")) :: Nil

homepage := Some(new java.net.URL("http://github.com/pawelkaczor/schale"))

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

publishMavenStyle := true

libraryDependencies ++= Seq(
	"com.typesafe.akka" %% "akka-actor" % "2.5.3",
	"org.scalatest" %% "scalatest" % "3.0.1" % "test"
)


Publish.settings