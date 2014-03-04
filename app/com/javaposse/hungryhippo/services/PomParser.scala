package com.javaposse.hungryhippo.services

import scala.xml.{NodeSeq, Elem, XML}
import com.javaposse.hungryhippo.models.{Module, Coordinate}


class PomParser {

    def parsePom(repoUrl: String, pomText: String) = {
        val pomXml: Elem = XML.loadString(pomText)

        val coordinate: Coordinate = parseCoordinate(pomXml)

        val dependencies = (pomXml \ "dependencies" \ "dependency").map{ dep =>
            parseCoordinate(dep)
        }

        Module(coordinate, repoUrl,  dependencies)
    }

    def parseCoordinate(coordNode: NodeSeq): Coordinate = {

        val groupId = (coordNode \ "groupId").text
        val artifactId = (coordNode \ "artifactId").text
        val version = (coordNode \ "version").text

        Coordinate(groupId, artifactId, version)
    }
}
