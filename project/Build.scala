import sbt._
import Keys._

// allows projects to be symlinked into the current directory for a direct dependency,
// or fall back to obtaining the project from Maven otherwise
class Local (locals :(String, String, ModuleID)*) {
  def addDeps (p :Project) = (locals collect {
    case (id, subp, dep) if (file(id).exists) => symproj(file(id), subp)
  }).foldLeft(p) { _ dependsOn _ }
  def libDeps = locals collect {
    case (id, subp, dep) if (!file(id).exists) => dep
  }
  private def symproj (dir :File, subproj :String = null) =
    if (subproj == null) RootProject(dir) else ProjectRef(dir, subproj)
}

object TriplePlayBuild extends Build {
  val locals = new Local(
    ("pythagoras", null,  "com.samskivert" % "pythagoras" % "1.1-SNAPSHOT"),
    ("react",      null,  "com.threerings" % "react" % "1.0-SNAPSHOT"),
    ("forplay",   "core", "com.googlecode.forplay" % "core" % "1.0-SNAPSHOT")
  )

  lazy val tripleplay = locals.addDeps(Project(
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
      libraryDependencies ++= locals.libDeps ++ Seq(
        // test dependencies
        "junit" % "junit" % "4.+" % "test",
 	      "com.novocode" % "junit-interface" % "0.7" % "test->default"
      )
    )
  ))
}
