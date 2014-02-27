package com.javaposse.hungryhippo.actors

import akka.actor.{PoisonPill, Props, Actor}
import play.api.Play
import play.api.libs.concurrent.Akka
import play.api.Play.current
import akka.routing.RoundRobinRouter
import scala.collection.JavaConversions._
import akka.actor.SupervisorStrategy.Stop

sealed abstract class CrawlControlMessage
case object StartCrawling extends CrawlControlMessage
case object StopCrawling extends CrawlControlMessage
case object CrawlState extends CrawlControlMessage

sealed abstract class ControllerState
case object Started extends ControllerState
case object Stopped extends ControllerState

class CrawlControlActor extends Actor {

  private var state: ControllerState = Stopped

  override def receive = {
    case CrawlState =>
      sender ! state
    case StartCrawling if (state == Stopped) => {
      state = Started

      // Create actors for CrawlDirectoryActor, LoadCoordianteActor, ParseMavenMetadataActor
      val crawlDirectoryRouter = Akka.system.actorOf(Props[CrawlDirectoryActor].withRouter(
        RoundRobinRouter(nrOfInstances = 1)), "crawlDirectory")
      val loadCoordinateRouter = Akka.system.actorOf(Props[LoadCoordinateActor].withRouter(
        RoundRobinRouter(nrOfInstances = 1)), "loadCoordinate")
      val parseMavenMetadataRouter = Akka.system.actorOf(Props[ParseMavenMetadataActor].withRouter(
        RoundRobinRouter(nrOfInstances = 1)), "parseMavenMetadata")

      Play.current.configuration.getStringList("crawl.roots") match {
        case Some(x) =>
          x foreach {
            context.actorSelection("/user/crawlDirectory") ! CrawlDirectory(_, "")
          }
        case None => throw new Exception("crawl.roots is undefined")
      }
    }
    case StopCrawling if (state == Started) => {
      state = Stopped
      // Shutdown routers
      context.actorSelection("/user/crawlDirectory") ! PoisonPill
      context.actorSelection("/user/loadCoordinate") !  PoisonPill
      context.actorSelection("/user/parseMavenMetadata") ! PoisonPill

    }
    case _ =>
      context.system.log.info("do what now?")
  }
}

