import play.sbt.PlayScala

name := """play-scala"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"
logLevel in compile := Level.Warn


libraryDependencies ++= Seq(
	jdbc,
	cache,
	ws,
	"commons-cli" % "commons-cli" % "1.3.1",
	"org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test,
	"org.scalamock" % "scalamock-scalatest-support_2.11" % "3.2.2",
	"org.scalactic" %% "scalactic" % "2.2.6",
	"org.scalatest" %% "scalatest" % "2.2.6" % "test",
	"com.typesafe.akka" %% "akka-cluster" % "2.4.9-RC2",
	"com.typesafe.akka" %% "akka-cluster-metrics" % "2.4.9-RC2",
	"com.typesafe.akka" %% "akka-remote" % "2.4.9-RC2"//,
//	"io.kamon" % "sigar-loader" % "1.6.6-rev002"
//	"org.hyperic" % "sigar" % "1.6.4" from "file://./lib/sigar.jar"
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

fork in run := true