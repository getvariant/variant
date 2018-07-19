//
// Variant Server build config
//

val coreVersion = "0.9.1"
name := "Variant"
version := coreVersion

lazy val root = (project in file(".")).enablePlugins(PlayScala)
scalaVersion := "2.11.7"

// Add local Maven repo for com.variant artifacts built with Maven.
resolvers += Resolver.mavenLocal

// This 
//resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

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
  
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test,
  
  // Variant Core
  "com.variant"            % "variant-core"        % coreVersion,  
  // Postgres 9.1 JDBC driver in test
  "postgresql"             % "postgresql"  % "9.1-901-1.jdbc4"   , //% Test, Include in production. #149
  // H2 In mem DB in test 
  "com.h2database"         % "h2"          % "1.4.191"           % Test,
  
  // Reflections class path scanner. As of May '17 provided with the WTFPL license.
  // "org.reflections"        % "reflections" % "0.9.11",

  // Need to install CORS filter
  filters
  )

// Capture SBT build info in a source file available at compile time.
sourceGenerators in Compile <+= (sourceManaged in Compile, version, name) map { (d, v, n) =>
  val file = d / "SbtService.scala"
  IO.write(file, """package com.variant.server.boot
    |object SbtService {
    |  val version = "%s"
    |  val name = "%s"
    |}
    |""".stripMargin.format(v, n))
  Seq(file)
}

//
// ScalaTest related settings
//

//fork := true  // without this JVM options won't hold

testOptions in Test += Tests.Argument("-oF")  // Full stack traces (truncagted by default)

// Test scoped classpath directory - need this for tests that deploy schema from classpath.
unmanagedClasspath in Test += baseDirectory.value / "conf-test"
unmanagedClasspath in Runtime += baseDirectory.value / "conf-test"

// Config overrides for run and test.
javaOptions in Test += "-Dvariant.config.file=conf-test/variant.conf"
javaOptions in Runtime += "-Dvariant.config.file=conf-test/variant.conf"

// To debug, uncomment and connect with eclipse after the VM is suspended.
// javaOptions in Test ++= Seq("-Xdebug",  "-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000")
