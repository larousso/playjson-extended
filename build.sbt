import sbtrelease.ReleaseStateTransformations._

lazy val root = (project in file("."))
  .enablePlugins(BuildInfoPlugin, GitVersioning, GitBranchPrompt)
  .settings(
    name := """playjson-extended""",
    organization := "com.adelegue",
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

lazy val publishSettings =
    Seq(
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
      developers := List(
        Developer("alexandre.delegue", "Alexandre Delègue", "", url(s"https://github.com/larousso"))
      ),
      publishMavenStyle := true,
      publishArtifact in Test := false,
      bintrayVcsUrl := Some(s"scm:git:git@github.com:$githubRepo.git"),
      bintrayCredentialsFile := file(".credentials"),
      pomIncludeRepository := { _ => false }
    )

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  setNextVersion,
  commitNextVersion,
  pushChanges)