// we use pom-util to read metadata from the Maven POMs
libraryDependencies += "com.samskivert" % "sbt-pom-util" % "0.6-SNAPSHOT"

// we use proguard to package up our tools
resolvers += Resolver.url(
  "sbt-plugin-releases-scalasbt",
  url("http://repo.scala-sbt.org/scalasbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns)

addSbtPlugin("org.scala-sbt" % "xsbt-proguard-plugin" % "0.1.3")

// this is needed to wire up LWJGL when running the java version
addSbtPlugin("com.github.philcali" % "sbt-lwjgl-plugin" % "3.1.4")
