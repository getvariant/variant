//
// Variant Server build config
//

scalaVersion := "2.12.8"

val coreVersion = "0.10.0"
name := "Variant"
version := coreVersion

lazy val root = (project in file(".")).enablePlugins(PlayScala)

// Add local Maven repo for com.variant artifacts built with Maven.
resolvers += Resolver.mavenLocal

libraryDependencies ++= Seq(
  
  // Required by Play!
  jdbc,
  //cache,
  ws,
  guice,  // Separate dependency as of Play 2.6
  openId,  // Separate dependency as of Play 2.6
  openId,  // Separate dependency as of Play 2.6
  
  "com.typesafe.play"      %% "play-json"          % "2.6.0",
  "com.typesafe.play"      %% "play-iteratees"     % "2.6.1",
  
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.0.0" % Test,
  
  // Variant Core
  "com.variant"            % "variant-core"        % coreVersion,  
  
  // We should not need this any more because there's no JDBC dependencies in app
  // and those in test are resolved at run time via the unmanaged /ext path.
  // Postgres 9.1 JDBC driver in test
  //"postgresql"             % "postgresql"  % "9.1-901-1.jdbc4"   , % Test
  
  // H2 In mem DB in test 
  "com.h2database"         % "h2"          % "1.4.191"           % Test,
  
  // Need to install CORS filter
  filters
  )

// Capture SBT build info in a source file available at compile time.
/** No longer works in sbt 13.******************\
sourceGenerators in Compile += (sourceManaged in Compile, version, name) map { (d, v, n) =>
  val file = d / "SbtService.scala"
  IO.write(file, """package com.variant.server.boot
    |object SbtService {
    |  val version = "%s"
    |  val name = "%s"
    |}
    |""".stripMargin.format(v, n))
  Seq(file)
}
*************************************************/

// Disable scaladoc in 'dist' task.
sources in (Compile,doc) := Seq.empty
publishArtifact in (Compile, packageDoc) := false

//
// ScalaTest related settings
//

//fork := true  // without this JVM options won't hold

// Full stack traces (truncated by default)
testOptions in Test += Tests.Argument("-oF") 

// Test scoped classpath directory - need this for tests that deploy schema from classpath.
unmanagedClasspath in Test += baseDirectory.value / "conf-test"
unmanagedClasspath in Runtime += baseDirectory.value / "conf-test"

// Java sources compilation level
javacOptions ++= Seq("-source", "1.8", "-target", "1.8")

// Include the standard extension for tests to see the flusher classes.
unmanagedClasspath in Test += baseDirectory.value / "distr/ext/*"
//unmanagedClasspath in Runtime += baseDirectory.value / "distr/ext/*"


// Config overrides for run and test.
javaOptions in Test += "-Dvariant.config.file=conf-test/variant.conf"
javaOptions in Runtime += "-Dvariant.config.file=conf-test/variant.conf"

// To debug, uncomment and connect with eclipse after the VM is suspended.
// javaOptions in Test ++= Seq("-Xdebug",  "-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000")
