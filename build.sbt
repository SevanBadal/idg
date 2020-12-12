ThisBuild / organization  := "org.pchapin"
ThisBuild / version       := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion  := "2.11.8"
ThisBuild / scalacOptions :=
  Seq("-encoding", "UTF-8",
      "-feature",
      "-deprecation",
      "-unchecked",
      "-Ywarn-adapted-args",
      "-Ywarn-dead-code",
      "-Ywarn-infer-any",
      "-Ywarn-unused-import",
      "-Ywarn-value-discard")

libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.5"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % "test"
libraryDependencies += "org.apache.hadoop" % "hadoop-common" % "3.1.1" % "provided"

logBuffered in Test := false

lazy val idg = (project in file("."))
  .settings(
    name := "IDG"
  )