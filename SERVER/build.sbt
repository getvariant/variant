/*
 * Server SBT build.
 */
 
import NativePackagerHelper._

maintainer := "igor@getvariant.com"
organization    := "com.variant"
scalaVersion    := "2.12.7"
name            := "Variant-Server"
version         := "0.10.3"

val product = "Variant AIM Server"
val akkaHttpVersion = "10.1.8"
val akkaVersion     = "2.5.25"

// Add local Maven repo for com.variant.core artifacts built with Maven.
resolvers += Resolver.mavenLocal

enablePlugins(JavaServerAppPackaging, BuildInfoPlugin)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http"            % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-xml"        % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-stream"          % akkaVersion,
  "com.typesafe.play" %% "play-json"            % "2.7.0",

  "com.typesafe.akka" %% "akka-http-testkit"    % akkaHttpVersion % Test,
  "com.typesafe.akka" %% "akka-testkit"         % akkaVersion     % Test,
  "com.typesafe.akka" %% "akka-stream-testkit"  % akkaVersion     % Test,
  "org.scalatest"     %% "scalatest"            % "3.0.5"         % Test,
  
  "ch.qos.logback"             % "logback-classic"  % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging"    % "3.9.2",
 
   // H2 In mem DB in test 
  "com.h2database"     % "h2"                   % "1.4.191"       % Test,
  
  // Variant Shared Lib
  "com.variant"            % "variant-share"        % "0.10.3"
  
)

// sbt-buildinfo plugin configuration
buildInfoKeys := Seq[BuildInfoKey](moduleName, version, scalaVersion, sbtVersion)
//buildInfoOptions := Seq(BuildInfoOption.BuildTime)
buildInfoPackage := "com.variant.server.build"
buildInfoKeys += BuildInfoKey.action("javaVersion")(sys.props("java.version"))
buildInfoKeys += BuildInfoKey.action("javaVmName")(sys.props("java.vm.name"))
buildInfoKeys += BuildInfoKey.action("javaVmVersion")(sys.props("java.vm.version"))
buildInfoKeys += BuildInfoKey.action("product")(product)
buildInfoKeys += BuildInfoKey.action("buildTimestamp")(java.time.Instant.now.toString)

// ...and prepend it to the startup script's classpath.
scriptClasspath := Seq("../conf/") ++ scriptClasspath.value

// ...and add ext/ to script's classpath.
scriptClasspath := Seq("../ext/*") ++ scriptClasspath.value

// Disable scaladoc generation for packaging
publishArtifact in (Compile, packageDoc) := false

// Rename the executable script, so that our own wrapper can be called 'variant'
executableScriptName := "variant-ctl"

//
// Test related settings.
//
// Change current directory to `test-base` for `test`, `testQuick`, and `testOnly`
// For this to work, fork must be set to true, i.e. each run is in a separate JVM
fork := true
Test / baseDirectory := file("test-base")
//baseDirectory in run := file("test-base")

// Do not truncate stack traces. (Use when needed for debugging)
// testOptions in Test += Tests.Argument("-oF")

// Append production resource directory "conf" so that its content is available
// but behind the content of src/test/resources
//unmanagedClasspath in Test += baseDirectory.value / "src" / "universal" / "conf"
unmanagedClasspath in Test += baseDirectory.value / "src" / "universal" / "ext/*"

// To debug, uncomment and connect with eclipse after the VM is suspended.
//javaOptions in Test ++= Seq("-Xdebug",  "-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000")

