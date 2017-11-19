import sbt._
import Keys._

name := "pipeflow"

lazy val defaultSettings = Defaults.coreDefaultSettings ++ Seq(
  scalaVersion := "2.10.6",
  crossScalaVersions := Seq("2.10.6", "2.11.11", "2.12.4"),

  organization := "org.pipeflow",
  version := "0.1",

  libraryDependencies ++= Seq(
    "com.typesafe" % "config" % "1.3.2",
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
      "com.assembla.scala-incubator" %% "graph-core" % "1.10.0"
    )
  ))

lazy val examples = Project(id = "pipeflow-examples", base = file("examples"))
  .settings(defaultSettings ++ Seq(
    name := "PipeFlow Examples",
    version := version.value,
    libraryDependencies ++= Seq(
    )
  ))
  .dependsOn(core)
