//
// Variant Server build config
//

val coreVersion = "0.7.0"
name := s"Variant Server $coreVersion"

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

// This doesn't work under eclipse => had to switch to building applicaiton programmatically with GuiceApplicationBuilder
//javaOptions in Test += "-Dconfig.resource=application.test.conf"
//javaOptions in Test +="-Dlogger.resource=test-logback.xml"

//fork in run := true