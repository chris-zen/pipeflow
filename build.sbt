import sbt._
import Keys._

name := "pipeflow"

lazy val akkaVersion = "2.3.16"

lazy val defaultSettings = Defaults.coreDefaultSettings ++ Seq(
  scalaVersion := "2.10.6",
  crossScalaVersions := Seq("2.10.6", "2.11.11", "2.12.4"),

  organization := "com.github.chris-zen.pipeflow",
  version := "0.1",

  libraryDependencies ++= Seq(
    "com.typesafe" % "config" % "1.3.2",
    "org.slf4j" % "slf4j-log4j12" % "1.7.25",
    "org.scalatest" %% "scalatest" % "3.0.4" % "test"
  )
)

lazy val all = Project(id = "pipeflow-all", base = file("."))
  .settings(
    name := "PipeFlow",
    publishTo := None)
  .aggregate(core, examples)


lazy val core = Project(id = "pipeflow-core", base = file("core"))
  .settings(defaultSettings ++ Seq(
    name := "PipeFlow Core",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
      "com.assembla.scala-incubator" %% "graph-core" % "1.10.0"
    )
  ))

lazy val examples = Project(id = "pipeflow-examples", base = file("examples"))
  .settings(defaultSettings ++ Seq(
    name := "PipeFlow Examples",
    version := version.value,
    mainClass := Some("pipeflow.examples.example1.Example1"),
    libraryDependencies ++= Seq(
    )
  ))
  .dependsOn(core)
