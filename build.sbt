seq(samskivert.POMUtil.pomToSettings("pom.xml") :_*)

crossPaths := false

scalaVersion := "2.9.1"

autoScalaLibrary := false // no scala-library dependency

javacOptions ++= Seq("-Xlint", "-Xlint:-serial", "-source", "1.6", "-target", "1.6")

fork in Compile := true

// include source in our jar for GWT
unmanagedResourceDirectories in Compile <+= baseDirectory / "src/main/java"

// disable doc publishing, TODO: reenable when scaladoc doesn't choke on our code
publishArtifact in (Compile, packageDoc) := false

// allows SBT to run junit tests
libraryDependencies += "com.novocode" % "junit-interface" % "0.7" % "test->default"
