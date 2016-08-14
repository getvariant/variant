name := "Variant Server"
val coreVersion = "0.6.1"
version := coreVersion
organization := "com.variant"

scalaVersion := "2.11.5"

// Add local Maven repo for com.variant artifacts built with Maven.
resolvers += Resolver.mavenLocal

resolvers ++= Seq("snapshots"     at "https://oss.sonatype.org/content/repositories/snapshots",
                "releases"        at "https://oss.sonatype.org/content/repositories/releases"
                )

seq(webSettings :_*)

unmanagedResourceDirectories in Test <+= (baseDirectory) { _ / "src/main/webapp" }

scalacOptions ++= Seq("-deprecation", "-unchecked")

libraryDependencies ++= {
  val liftVersion = "2.6.2"
  
  Seq(
    "net.liftweb"       %% "lift-webkit"        % liftVersion        % "compile",
    "net.liftweb"       %% "lift-testkit"       % liftVersion        % "test",

    "org.eclipse.jetty" % "jetty-webapp"        % "8.1.17.v20150415"  % "container,test",
//    "org.eclipse.jetty" % "jetty-servlets"      % "8.1.17.v20150415",                     // CrossOriginFilter
    "org.eclipse.jetty" % "jetty-plus"          % "8.1.17.v20150415"  % "container,test", // For Jetty Config
    "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "container,test" artifacts Artifact("javax.servlet", "jar", "jar"),

    "ch.qos.logback"    % "logback-classic"     % "1.1.3",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
//    "org.specs2"        %% "specs2-core"        % "3.6.4"           % "test",

    "com.h2database"    % "h2"                  % "1.4.191"           % "test",  // We need the latest
    "postgresql"        % "postgresql"          % "9.1-901-1.jdbc4",

    "org.scalatest" %% "scalatest" % "2.2.6" % "test",
    "org.apache.httpcomponents" % "httpcore"            % "4.4.4",
    
    // Variant
    "com.variant"               % "variant-core"        % coreVersion
  )
}

//parallelExecution in Test := false          // Run suites one at a time because there's only one Jetty instance per JVM -- does not seem to work
testOptions in Test += Tests.Argument("-oF")  // Do not truncate stack traces.
scalacOptions in Test ++= Seq("-Yrangepos")   // ?
EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource // Tells sbteclipse to include reource dirs in eclipse classpaths.

env in Compile := Some(file(".") / "jetty-env.xml" asFile)
