package com.javaposse.hungryhippo.actors

import akka.actor.ActorRef
import akka.actor.{PoisonPill, Props, Actor}
import akka.routing.RoundRobinRouter
import play.api.Play
import play.api.Play.current
import play.api.libs.concurrent.Akka
import scala.collection.JavaConversions._
import com.javaposse.hungryhippo.event.Events.CrawlStateChanged

sealed abstract class CrawlControlMessage
case object StartCrawling extends CrawlControlMessage
case object StopCrawling extends CrawlControlMessage
case object GetCrawlStatus extends CrawlControlMessage

sealed abstract class CrawlState
case object Started extends CrawlState
case object Stopped extends CrawlState

class CrawlControlActor(notificationActor: ActorRef) extends Actor {

  private var state: CrawlState = Stopped

  override def receive = {
    case StartCrawling if (state == Stopped) =>
      state = Started
      notificationActor ! CrawlStateChanged(state)

      // Create actors for CrawlDirectoryActor, LoadCoordianteActor, ParseMavenMetadataActor
      val crawlDirectoryRouter = context.actorOf(Props[CrawlDirectoryActor].withRouter(
        RoundRobinRouter(nrOfInstances = 5)), "crawlDirectory")
      val loadCoordinateRouter = Akka.system.actorOf(Props[LoadCoordinateActor].withRouter(
        RoundRobinRouter(nrOfInstances = 5)), "loadCoordinate")
      val parseMavenMetadataRouter = Akka.system.actorOf(Props[ParseMavenMetadataActor].withRouter(
        RoundRobinRouter(nrOfInstances = 5)), "parseMavenMetadata")

      Play.current.configuration.getStringList("crawl.roots") match {
        case Some(x) =>
          x foreach {
            context.actorSelection("/user/crawlDirectory") ! CrawlDirectory(_, "")
          }
        case None => throw new Exception("crawl.roots is undefined")
      }
    case StopCrawling if (state == Started) =>
      state = Stopped
      notificationActor ! CrawlStateChanged(state)

      // Shutdown routers
      context.actorSelection("/user/crawlDirectory") ! PoisonPill
      context.actorSelection("/user/loadCoordinate") !  PoisonPill
      context.actorSelection("/user/parseMavenMetadata") ! PoisonPill
    case GetCrawlStatus =>
      sender ! state
    case _ =>
      context.system.log.info("do what now?")
  }
}

