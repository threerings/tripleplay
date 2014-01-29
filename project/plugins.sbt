// we use pom-util to read metadata from the Maven POMs
libraryDependencies += "com.samskivert" % "sbt-pom-util" % "0.6"

// this wires up JRebel; start demo with JRebel via: demo-java/re-start
addSbtPlugin("io.spray" % "sbt-revolver" % "0.7.1")

// we use proguard to package up our tools
// resolvers += Resolver.url(
//   "sbt-plugin-releases-scalasbt",
//   url("http://repo.scala-sbt.org/scalasbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns)

// addSbtPlugin("org.scala-sbt" % "xsbt-proguard-plugin" % "0.1.3")
