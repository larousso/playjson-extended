import sbtrelease.ReleaseStateTransformations._

val disabledPlugins = if (sys.env.get("TRAVIS_TAG").filterNot(_.isEmpty).isDefined) {
  Seq()
} else {
  Seq(BintrayPlugin)
}

lazy val root = (project in file("."))
  .enablePlugins(GitVersioning, GitBranchPrompt)
  .disablePlugins(disabledPlugins:_*)
  .settings(
    name := """playjson-extended""",
    organization := "com.adelegue",
    resolvers += Resolver.jcenterRepo,
    scalaVersion := "2.12.8",
    releaseCrossBuild := true,
    libraryDependencies ++= Seq(
      "com.typesafe.play" %% "play-json" % "2.7.1",
      "com.chuusai"       %% "shapeless" % "2.3.3",
      "org.scalatest"     %% "scalatest" % "3.0.4" % Test
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
  developers := List(
    Developer("alexandre.delegue", "Alexandre Delègue", "", url(s"https://github.com/larousso"))
  ),
  publishMavenStyle := true,
  publishArtifact in Test := false,
  bintrayVcsUrl := Some(s"scm:git:git@github.com:$githubRepo.git")
)

lazy val publishSettings =
  if (sys.env.get("TRAVIS_TAG").filterNot(_.isEmpty).isDefined) {
    publishCommonsSettings ++ Seq(
      bintrayCredentialsFile := file(".credentials"),
      pomIncludeRepository := { _ =>
        false
      }
    )
  } else {
    publishCommonsSettings ++ Seq(
      publishTo := Some(
        "Artifactory Realm" at "http://oss.jfrog.org/artifactory/oss-snapshot-local"
      ),
      bintrayReleaseOnPublish := false,
      credentials := List(
        Credentials("Artifactory Realm", "oss.jfrog.org", sys.env.getOrElse("BINTRAY_USER", ""), sys.env.getOrElse("BINTRAY_PASSWORD", ""))
      )
    )
  }



releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  setNextVersion,
  commitNextVersion,
  pushChanges)