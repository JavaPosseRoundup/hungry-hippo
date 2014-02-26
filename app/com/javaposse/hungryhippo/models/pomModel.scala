package com.javaposse.hungryhippo.models

case class Coordinate(groupId: String, artifactId: String, version: Option[String])

case class Module(
    id: Coordinate,
    repoUrl: String,
    dependencies: Seq[Coordinate]
)

