import sbt._
import Keys._
import samskivert.ProjectBuilder

object TriplePlayBuild extends Build {
  val builder = new ProjectBuilder("pom.xml") {
    override val globalSettings = Seq(
      crossPaths    := false,
      scalaVersion  := "2.9.1",
      javacOptions  ++= Seq("-Xlint", "-Xlint:-serial", "-source", "1.6", "-target", "1.6"),
      scalacOptions ++= Seq("-unchecked", "-deprecation"),
      javaOptions   ++= Seq("-ea"),
      fork in Compile := true
    )
    override def projectSettings (name :String) = name match {
      case "core" => LWJGLPlugin.lwjglSettings ++ seq(
        // no scala-library dependency here
        autoScalaLibrary := false,
        // include source in our jar for GWT
        unmanagedResourceDirectories in Compile <+= baseDirectory / "src/main/java",
        // disable doc publishing, TODO: reenable when scaladoc doesn't choke on our code
        publishArtifact in (Compile, packageDoc) := false,
        libraryDependencies ++= Seq(
          // TODO: this shouldn't be necessary
          // "com.googlecode.playn" % "playn-java" % "1.2-SNAPSHOT",
          // scala test dependencies
	        "com.novocode" % "junit-interface" % "0.7" % "test->default"
        )
      )
      // case "tools" => seq(
      //   libraryDependencies ++= Seq(
      //     "com.novocode" % "junit-interface" % "0.7" % "test->default"
      //   )
      // )
      case _ => Nil
    }
  }

  lazy val core = builder("core")
  lazy val tools = builder("tools")

  // one giant fruit roll-up to bring them all together
  lazy val tripleplay = Project("tripleplay", file(".")) aggregate(core, tools)
}
