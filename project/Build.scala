import sbt._
import Keys._

object TriplePlayBuild extends Build {
  val locals = com.samskivert.condep.Depends(
    ("react",      null,  "com.threerings" % "react" % "1.0-SNAPSHOT"),
    ("playn",     "core", "com.googlecode.playn" % "playn-core" % "1.0-SNAPSHOT"),
    ("playn",     "java", "com.googlecode.playn" % "playn-java" % "1.0-SNAPSHOT" % "test")
  )

  lazy val tripleplay = locals.addDeps(Project(
    "tripleplay", file("."), settings = Defaults.defaultSettings ++ Seq(
      organization := "com.threerings",
      version      := "1.0-SNAPSHOT",
      name         := "tripleplay",
      crossPaths   := false,
      scalaVersion := "2.9.0-1",

      javacOptions ++= Seq("-Xlint", "-Xlint:-serial", "-source", "1.6", "-target", "1.6"),
      fork in Compile := true,

      // TODO: reenable doc publishing when scaladoc doesn't choke on our code
      publishArtifact in (Compile, packageDoc) := false,

      // add our sources to the main jar file
      unmanagedResourceDirectories in Compile <+= baseDirectory / "src/main/java",

      autoScalaLibrary := false, // no scala-library dependency
      libraryDependencies ++= locals.libDeps ++ Seq(
        // test dependencies
        "junit" % "junit" % "4.+" % "test",
 	      "com.novocode" % "junit-interface" % "0.7" % "test->default"
      )
    )
  ))
}
