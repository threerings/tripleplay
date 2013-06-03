import sbt._
import Keys._
import ProguardPlugin._

object TriplePlayBuild extends samskivert.MavenBuild {

  override val globalSettings = Seq(
    crossPaths    := false,
    scalaVersion  := "2.10.0",
    javacOptions  ++= Seq("-Xlint", "-Xlint:-serial", "-source", "1.6", "-target", "1.6"),
    scalacOptions ++= Seq("-unchecked", "-deprecation"),
    javaOptions   ++= Seq("-ea"),
    // disable doc publishing, TODO: reenable when scaladoc doesn't choke on our code
    publishArtifact in (Compile, packageDoc) := false,
    fork in Compile := true
  )

  override def moduleSettings (name :String, pom :pomutil.POM) = name match {
    case "core" => seq(
      // no scala-library dependency here
      autoScalaLibrary := false,
      // include source in our jar for GWT
      unmanagedResourceDirectories in Compile <+= baseDirectory / "src/main/java",
      // wire junit into SBT
      libraryDependencies ++= Seq(
        "com.novocode" % "junit-interface" % "0.7" % "test->default"
      )
    )
    case "tools" => proguardSettings ++ seq(
      mainClass in (Compile, run) := Some("tripleplay.tools.FramePacker"),
      proguardOptions += keepMain("tripleplay.tools.FramePacker"),
      proguardOptions += "-dontnote scala.Enumeration"
    )
    case "demo-core" => Seq(
      // copy resources from playn/tests/resources
      unmanagedResourceDirectories in Compile <+= baseDirectory / "src/main/java",
      excludeFilter in unmanagedResources ~= { _ || "*.java" },
      publish := false,
      publishLocal := false
    )
    case "demo-java" => LWJGLPlugin.lwjglSettings ++ Seq(
      publish := false,
      publishLocal := false,
      LWJGLPlugin.lwjgl.version := pom.getAttr("lwjgl.version").get
    )
    case _ => Nil
  }

  override protected def projects (builder :samskivert.ProjectBuilder) =
    super.projects(builder) ++ Seq(builder("demo-core"), builder("demo-java"))
}
