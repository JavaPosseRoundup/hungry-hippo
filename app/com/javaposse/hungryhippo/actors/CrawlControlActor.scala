package com.javaposse.hungryhippo.actors

import akka.actor.ActorRef
import akka.actor.{PoisonPill, Props, Actor}
import akka.routing.RoundRobinRouter
import play.api.Play
import play.api.Play.current
import play.api.libs.concurrent.Akka
import scala.collection.JavaConversions._
import scala.collection.mutable
import play.Logger

sealed abstract class CrawlControlMessage
case object StartCrawling extends CrawlControlMessage
case object StopCrawling extends CrawlControlMessage
case object CrawlState extends CrawlControlMessage
case object WatchCrawlState extends CrawlControlMessage
case object UnwatchCrawlState extends CrawlControlMessage

sealed abstract class ControllerState
case object Started extends ControllerState
case object Stopped extends ControllerState

class CrawlControlActor extends Actor {

  private var state: ControllerState = Stopped
  private val watchers: mutable.Set[ActorRef] = mutable.HashSet()

  override def receive = {
    case CrawlState =>
      sender ! state
    case StartCrawling if (state == Stopped) => {
      state = Started
      updateWatchers

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
      updateWatchers

      // Shutdown routers
      context.actorSelection("/user/crawlDirectory") ! PoisonPill
      context.actorSelection("/user/loadCoordinate") !  PoisonPill
      context.actorSelection("/user/parseMavenMetadata") ! PoisonPill

    }
    case WatchCrawlState =>
      Logger.info("adding actor to watch crawl state")
      watchers += sender
    case UnwatchCrawlState =>
      watchers -= sender
    case _ =>
      context.system.log.info("do what now?")
  }

  def updateWatchers = {
    watchers.foreach{ _ ! StateChanged(state)}
  }
}

