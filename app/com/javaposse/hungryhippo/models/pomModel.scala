package com.javaposse.hungryhippo.models

case class ModuleId(groupId: String, artifactId: String, version: Option[String])

case class Module(
    id: ModuleId,
    repoUrl: String,
    dependencies: Seq[ModuleId]
)

