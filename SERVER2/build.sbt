/*
 * Server SBT build.
 */
import scalariform.formatter.preferences._

organization    := "com.variant"
scalaVersion    := "2.12.7"
name            := "Variant"

val akkaHttpVersion = "10.1.8"
val akkaVersion     = "2.6.0-M4"

enablePlugins(JavaAppPackaging)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http"            % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-xml"        % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-stream"          % akkaVersion,

  "com.typesafe.akka" %% "akka-http-testkit"    % akkaHttpVersion % Test,
  "com.typesafe.akka" %% "akka-testkit"         % akkaVersion     % Test,
  "com.typesafe.akka" %% "akka-stream-testkit"  % akkaVersion     % Test,
  "org.scalatest"     %% "scalatest"            % "3.0.5"         % Test
)

// Scalariform plugin enforces formatting convention on all dirty files which required recompilation.
// The following overrides default formatter settings. The plugin always fills indents with spaces.
// Be sure your local IDE settings are in sync.
scalariformPreferences := scalariformPreferences.value
    .setPreference(IndentSpaces, 3)
 