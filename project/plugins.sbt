// we use pom-util to read metadata from the Maven POMs
libraryDependencies += "com.samskivert" % "sbt-pom-util" % "0.3"

// we use proguard to package up our tools
libraryDependencies <+= sbtVersion(v => "com.github.siasia" %% "xsbt-proguard-plugin" % (v+"-0.1.1"))

// this is needed to wire up LWJGL when running the java version
addSbtPlugin("com.github.philcali" % "sbt-lwjgl-plugin" % "3.1.3")
