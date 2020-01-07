import sbt.project

ThisBuild / organization := "org.broadinstitute"
ThisBuild / version      := "0.0.2"
ThisBuild / scalaVersion := "2.13.1"
ThisBuild / scalacOptions ++= Seq("-feature", "-deprecation", "-unchecked")


val scalaTestV = "3.1.0"
val betterFilesV = "3.8.0"

lazy val core = (project in file("core"))
  .settings(
    name := "yootilz-core",
    libraryDependencies ++= Seq(
      "com.github.pathikrit" %% "better-files" % betterFilesV,
      "org.scalatest" %% "scalatest" % scalaTestV % "test"
    )
  )

