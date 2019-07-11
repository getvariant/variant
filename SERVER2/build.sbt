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

enablePlugins(JavaAppPackaging)

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
 
// Map resource directory to conf
resourceDirectory := baseDirectory.value / "main" / "universal" / "conf"

// ...and prepend it to the startup script's classpath.
scriptClasspath := Seq("../conf/") ++ scriptClasspath.value