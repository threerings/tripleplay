import sbt._
import Keys._

object TriplePlayBuild extends Build {
  // we do some jockeying here to allow the intrepid developer to symlink a checkout of pythagoras
  // and forplay into the current directory, in which case SBT is instructed to use those directly
  // as dependent projects (allowing automatic rebuild of files therein when, for example, running
  // test targets here in tripleplay); if one or the other symlink does not exist, it will obtain
  // those dependencies via Maven
  def findProject (name :String, depend :ModuleID, project : => ProjectReference) =
    if (new java.io.File(name).exists) (None, project)
    else                               (Some(depend), null)

  val (localDeps, localProjs) = Seq(
    findProject("pythagoras", "com.samskivert" % "pythagoras" % "1.1-SNAPSHOT",
                RootProject(file("pythagoras"))),
    findProject("forplay", "com.googlecode.forplay" % "core" % "1.0-SNAPSHOT",
                ProjectRef(file("forplay"), "core"))) unzip

  lazy val tripleplay = (Project(
    "tripleplay", file("."), settings = Defaults.defaultSettings ++ Seq(
      organization := "com.threerings",
      version      := "1.0-SNAPSHOT",
      name         := "triplelpay",
      crossPaths   := false,

      javacOptions ++= Seq("-Xlint", "-Xlint:-serial"),
      fork in Compile := true,

      resolvers += "Local Maven Repository" at Path.userHome.asURL + "/.m2/repository",

      libraryDependencies ++= localDeps.flatten ++ Seq(
        // test dependencies
        "junit" % "junit" % "4.+" % "test",
 	      "com.novocode" % "junit-interface" % "0.7" % "test->default"
      )
    )
  ) /: localProjs) { (p, lp) => if (lp == null) p else p dependsOn(lp) }
}
