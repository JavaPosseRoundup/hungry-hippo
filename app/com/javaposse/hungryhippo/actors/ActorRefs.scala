package com.javaposse.hungryhippo.actors

import akka.actor.{Props, ActorRef}
import play.api.libs.concurrent.Akka
import play.api.Play.current
import play.api.Play
import scala.collection.JavaConversions._


object ActorRefs {
  val NotificationActorName = "notification"
  lazy val notificationActor: ActorRef = Akka.system.actorOf(Props[NotificationActor], NotificationActorName)

  val CrawlControlActorName = "crawlController"
  lazy val crawlControlActor: ActorRef = Akka.system.actorOf(Props(classOf[CrawlControlActor], notificationActor,
    Play.current.configuration.getStringList("crawl.roots").map(_.toList)), CrawlControlActorName)
}
