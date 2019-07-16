/*
 * Server SBT build.
 */
 
 // Used by the Scalariform formatter
import scalariform.formatter.preferences._

import NativePackagerHelper._

organization    := "com.variant"
scalaVersion    := "2.12.7"
name            := "Variant"
version         := "0.10.1"

val akkaHttpVersion = "10.1.8"
val akkaVersion     = "2.6.0-M4"
val coreVersion     = "0.10.1"

// Add local Maven repo for com.variant.core artifacts built with Maven.
resolvers += Resolver.mavenLocal

enablePlugins(JavaServerAppPackaging)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http"            % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-xml"        % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-stream"          % akkaVersion,

  "com.typesafe.akka" %% "akka-http-testkit"    % akkaHttpVersion % Test,
  "com.typesafe.akka" %% "akka-testkit"         % akkaVersion     % Test,
  "com.typesafe.akka" %% "akka-stream-testkit"  % akkaVersion     % Test,
  "org.scalatest"     %% "scalatest"            % "3.0.5"         % Test,
  
  "ch.qos.logback"             % "logback-classic"  % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging"    % "3.9.2",
  
  // Variant Core
  "com.variant"            % "variant-core"        % coreVersion
  
)
    
// Scalariform plugin enforces formatting convention on all dirty files which required recompilation.
// The following overrides default formatter settings. The plugin always fills indents with spaces.
// Be sure your local IDE settings are in sync.
scalariformPreferences := scalariformPreferences.value
    .setPreference(IndentSpaces, 3)

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

// Append production resource directory "conf" so that its content is available
// but behind the content of src/test/resources
unmanagedClasspath in Test += baseDirectory.value / "src" / "universal" / "conf"
unmanagedClasspath in Test += baseDirectory.value / "src" / "universal" / "ext/*"

// Change current directory to `test-base` for `test`, `testQuick`, and `testOnly`
// For this to work, fork must be set to true, i.e. each run is in a separate JVM
fork := true
Test / baseDirectory := file("test-base")

