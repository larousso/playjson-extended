name := """playjson-extended"""
organization := "com.adelegue"

version := "0.0.2-SNAPSHOT"

crossScalaVersions := Seq("2.11.8", scalaVersion.value)

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-json" % "2.6.5",
  "com.chuusai"              %% "shapeless"           % "2.3.2",
  "org.scalatest" %% "scalatest" % "3.0.4" % Test
)

publishTo := {
  val localPublishRepo = "./repository"
  if (isSnapshot.value) {
    Some(Resolver.file("snapshots", new File(localPublishRepo + "/snapshots")))
  } else {
    Some(Resolver.file("releases", new File(localPublishRepo + "/releases")))
  }
}

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra := (
  <url>http://adelegue.org</url>
    <licenses>
      <license>
        <name>Apache 2</name>
        <url>http://www.apache.org/licenses/LICENSE-2.0</url>
        <distribution>repo</distribution>
      </license>
    </licenses>
    <developers>
      <developer>
        <id>alexandre.delegue</id>
        <name>Alexandre Delègue</name>
        <url>https://github.com/larousso</url>
      </developer>
    </developers>
  )