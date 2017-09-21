name := """playjson-extended"""
organization := "com.adelegue"

version := "0.0.1-SNAPSHOT"

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-json" % "2.6.5",
  "com.chuusai"              %% "shapeless"           % "2.3.2",
  "org.scalatest" %% "scalatest" % "3.0.4" % Test
)