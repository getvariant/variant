//
// Variant Server build config
//

val coreVersion = "0.7.0"
name := "Variant Experiment Server"
version := coreVersion

lazy val root = (project in file(".")).enablePlugins(PlayScala)
scalaVersion := "2.11.7"

// Add local Maven repo for com.variant artifacts built with Maven.
resolvers += Resolver.mavenLocal

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test,
  
  // Variant Core
  "com.variant"            % "variant-core"        % coreVersion,  
  // Postgres 9.1 JDBC driver in test
  "postgresql" % "postgresql" % "9.1-901-1.jdbc4" % Test,
  // H2 In mem DB in test 
  "com.h2database" % "h2"   % "1.4.191"         % Test
)

// Test scoped classpath directory - need this for tests that deploy schema from classpath.
unmanagedClasspath in Test += baseDirectory.value / "test-conf"
//unmanagedClasspath in Runtime += baseDirectory.value / "test-conf"

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

// Config overrides for run and test
javaOptions in Runtime  += "-Dvariant.config.file=test-conf/variant-test.conf"


//fork in run := true