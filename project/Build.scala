import sbt._
import Keys._

object TriplePlayBuild extends Build {
  // we do some jockeying here to allow the intrepid developer to symlink a checkout of pythagoras
  // and forplay into the current directory, in which case SBT is instructed to use those directly
  // as dependent projects (allowing automatic rebuild of files therein when, for example, running
  // test targets here in tripleplay); if one or the other symlink does not exist, it will obtain
  // those dependencies via Maven
  def findProject (name :String, depend :ModuleID, project : => ProjectReference) =
    if (new java.io.File(name).exists) (None, Some(project)) else (Some(depend), None)

  val (localDeps, localProjs) = Seq(
    findProject("react", "com.threerings" % "react" % "1.0-SNAPSHOT",
                RootProject(file("react"))),
    findProject("pythagoras", "com.samskivert" % "pythagoras" % "1.1-SNAPSHOT",
                RootProject(file("pythagoras"))),
    findProject("forplay", "com.googlecode.forplay" % "core" % "1.0-SNAPSHOT",
                ProjectRef(file("forplay"), "core"))
  ) unzip

  lazy val tripleplay = (Project(
    "tripleplay", file("."), settings = Defaults.defaultSettings ++ Seq(
      organization := "com.threerings",
      version      := "1.0-SNAPSHOT",
      name         := "tripleplay",
      crossPaths   := false,
      scalaVersion := "2.9.0-1",

      javacOptions ++= Seq("-Xlint", "-Xlint:-serial"),
      fork in Compile := true,

      // TODO: reenable doc publishing when scaladoc doesn't choke on our code
      publishArtifact in (Compile, packageDoc) := false,

      resolvers += "Local Maven Repository" at Path.userHome.asURL + "/.m2/repository",

      // this hackery causes publish-local to install to ~/.m2/repository instead of ~/.ivy
      otherResolvers := Seq(Resolver.file("dotM2", file(Path.userHome + "/.m2/repository"))),
      publishLocalConfiguration <<= (packagedArtifacts, deliverLocal, ivyLoggingLevel) map {
        (arts, _, level) => new PublishConfiguration(None, "dotM2", arts, level)
      },

      autoScalaLibrary := false, // no scala-library dependency
      libraryDependencies ++= localDeps.flatten ++ Seq(
        // test dependencies
        "junit" % "junit" % "4.+" % "test",
 	      "com.novocode" % "junit-interface" % "0.7" % "test->default"
      )
    )
  ) /: localProjs.flatten) { _ dependsOn _ }
}
