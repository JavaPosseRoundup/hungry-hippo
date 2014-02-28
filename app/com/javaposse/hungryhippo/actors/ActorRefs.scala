package com.javaposse.hungryhippo.actors

import akka.actor.{Props, ActorRef}
import play.api.libs.concurrent.Akka
import play.api.Play.current

object ActorRefs {
  val NotificationActorName = "notification"
  lazy val notificationActor: ActorRef = Akka.system.actorOf(Props[NotificationActor], NotificationActorName)

  val CrawlControlActorName = "crawlController"
  lazy val crawlControlActor: ActorRef = Akka.system.actorOf(Props(classOf[CrawlControlActor], notificationActor), CrawlControlActorName)
}
