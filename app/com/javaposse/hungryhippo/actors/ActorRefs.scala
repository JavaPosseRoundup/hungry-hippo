package com.javaposse.hungryhippo.actors

import akka.actor.{Props, ActorRef}
import play.api.libs.concurrent.Akka
import play.api.Play.current

object ActorRefs {
  val CrawlControlActorName = "crawlController"
  lazy val crawlControlActor: ActorRef = Akka.system.actorOf(Props[CrawlControlActor], CrawlControlActorName)

}
