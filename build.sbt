import sbt.project

ThisBuild / organization := "org.broadinstitute"
ThisBuild / version      := "0.1.0"
ThisBuild / scalaVersion := "2.13.1"
ThisBuild / scalacOptions ++= Seq("-feature", "-deprecation", "-unchecked")


val scalaTestV = "3.1.0"
val betterFilesV = "3.8.0"
val sttpV = "2.0.0-RC6"
val googleAuthV = "0.19.0"

lazy val core = (project in file("core"))
  .settings(
    name := "yootilz-core",
    libraryDependencies ++= Seq(
      "com.github.pathikrit" %% "better-files" % betterFilesV,
      "org.scalatest" %% "scalatest" % scalaTestV % "test"
    )
  )

lazy val gcp = (project in file("gcp")).dependsOn(core)
  .settings(
    name := "yootilz-gcp",
    libraryDependencies ++= Seq(
      "com.github.pathikrit" %% "better-files" % betterFilesV,
      "com.google.cloud" % "google-cloud-storage" % "1.103.0",
      "com.softwaremill.sttp.client" %% "core" % sttpV,
      "com.google.auth" % "google-auth-library-oauth2-http" % googleAuthV,
      "org.scalatest" %% "scalatest" % scalaTestV % "test"
    )
  )

