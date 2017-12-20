import sbtrelease._
import sbtrelease.ReleaseStateTransformations._

val currentVersion = "0.0.2-SNAPSHOT"

lazy val root = (project in file("."))
  .enablePlugins(BuildInfoPlugin, GitVersioning, GitBranchPrompt)
  .settings(
    name := """playjson-extended""",
    organization := "com.adelegue",
    version := currentVersion,
    resolvers += Resolver.jcenterRepo,
    releaseCrossBuild := true,
    crossScalaVersions := Seq("2.11.8", scalaVersion.value),
    libraryDependencies ++= Seq(
      "com.typesafe.play" %% "play-json" % "2.6.5",
      "com.chuusai"              %% "shapeless"           % "2.3.2",
      "org.scalatest" %% "scalatest" % "3.0.4" % Test
    )
  )
  .settings(publishSettings:_*)

lazy val githubRepo = "larousso/playjson-extended"

lazy val publishCommonsSettings = Seq(
  homepage := Some(url(s"https://github.com/$githubRepo")),
  startYear := Some(2017),
  licenses := Seq(("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))),
  scmInfo := Some(
    ScmInfo(
      url(s"https://github.com/$githubRepo"),
      s"scm:git:https://github.com/$githubRepo.git",
      Some(s"scm:git:git@github.com:$githubRepo.git")
    )
  ),
  publishMavenStyle := true,
  publishArtifact in Test := false,
  bintrayVcsUrl := Some(s"scm:git:git@github.com:$githubRepo.git"),
  pomExtra := (
    <url>http://adelegue.org</url>
      <licenses>
        <license>
          <name>Apache 2</name>
          <url>http://www.apache.org/licenses/LICENSE-2.0</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      <scm>
        <url>https://github.com/larousso/playjson-extended</url>
        <connection>https://github.com/larousso/playjson-extended</connection>
      </scm>
      <developers>
        <developer>
          <id>alexandre.delegue</id>
          <name>Alexandre Delègue</name>
          <url>https://github.com/larousso</url>
        </developer>
      </developers>
    )
)

lazy val publishSettings =
    publishCommonsSettings ++ Seq(
      bintrayCredentialsFile := file(".credentials"),
      //bintrayOrganization := Some("com.adelegue"),
      pomIncludeRepository := { _ => false }
      //resolvers += Resolver.url("Ivy resolver", url("http://dl.bintray.com/adelegue/maven"))(Resolver.mavenStylePatterns),
    )

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  releaseStepCommandAndRemaining("publish"),
  setNextVersion,
  commitNextVersion,
  pushChanges)