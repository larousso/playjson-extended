#!/usr/bin/env bash


if test "$TRAVIS_PULL_REQUEST" = "false"
then
    sbt ++$TRAVIS_SCALA_VERSION ";test;publish"
else
    sbt ++$TRAVIS_SCALA_VERSION ";test"
fi