package com.javaposse.hungryhippo.actors

import akka.actor.{Props, Actor}
import com.javaposse.hungryhippo.models.Module
import com.javaposse.hungryhippo.actors.PomParserActor.{ParsePomResponse, ParsePom}
import scala.util.Try
import com.javaposse.hungryhippo.services.PomParser

class PomParserActor extends Actor {

    val pomParser = new PomParser()

    def receive: Receive = {
        case ParsePom(repoUrl, pomText) =>
            sender ! ParsePomResponse(Try(pomParser.parsePom(repoUrl, pomText)))
    }
}

object PomParserActor {
    case class ParsePom(repoUrl: String, pomText: String)
    case class ParsePomResponse(module: Try[Module])

    val props = Props[PomParserActor]
}
