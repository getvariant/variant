//
// Variant Server build config
//

val coreVersion = "0.6.3"
name := s"Variant Server $coreVersion"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test,
  
    // Variant
    "com.variant"               % "variant-core"        % coreVersion
)

//fork in run := true