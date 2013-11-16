import sbt._
import Keys._
// import ProguardPlugin._

object TriplePlayBuild extends samskivert.MavenBuild {

  override val globalSettings = Seq(
    crossPaths    := false,
    scalaVersion  := "2.10.0",
    javacOptions  ++= Seq("-Xlint", "-Xlint:-serial", "-source", "1.6", "-target", "1.6"),
    scalacOptions ++= Seq("-unchecked", "-deprecation"),
    javaOptions   ++= Seq("-ea"),
    // disable doc publishing, TODO: reenable when scaladoc doesn't choke on our code
    publishArtifact in (Compile, packageDoc) := false,
    fork in Compile := true,
    // wire junit into SBT
    libraryDependencies ++= Seq(
      "com.novocode" % "junit-interface" % "0.10" % "test->default"
    ),
    testOptions += Tests.Argument(TestFrameworks.JUnit, "-a", "-v")
  )

  override def moduleSettings (name :String, pom :pomutil.POM) = name match {
    case "core" => seq(
      // no scala-library dependency here
      autoScalaLibrary := false,
      // include source in our jar for GWT
      unmanagedResourceDirectories in Compile <+= baseDirectory / "src/main/java"
    )
    // case "tools" => proguardSettings ++ seq(
    //   mainClass in (Compile, run) := Some("tripleplay.tools.FramePacker"),
    //   proguardOptions += keepMain("tripleplay.tools.FramePacker"),
    //   proguardOptions += "-dontnote scala.Enumeration"
    // )
    case name if (name startsWith "demo-") => seq(
      publish := (),
      publishLocal := ()
    )
    case _ => Nil
  }

  override protected def projects (builder :samskivert.ProjectBuilder) =
    super.projects(builder) ++ Seq(
      builder("demo-assets"), builder("demo-core"), builder("demo-java"))
}
