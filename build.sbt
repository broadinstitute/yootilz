import sbt.project

ThisBuild / organization := "org.broadinstitute"
ThisBuild / version      := "0.0.1"
ThisBuild / scalaVersion := "2.13.1"


val scalaTestV = "3.1.0"
val betterFilesV = "3.8.0"

lazy val mainDeps = Seq(
  "com.github.pathikrit" %% "better-files" % betterFilesV
)

lazy val testDeps = Set(
  "org.scalatest" %% "scalatest" % scalaTestV % "test"
)

lazy val core = (project in file("core"))
  .settings(
    name := "yootilz-core",
    libraryDependencies ++= (mainDeps ++ testDeps),
    scalacOptions ++= Seq("-feature", "-deprecation", "-unchecked")
  )

