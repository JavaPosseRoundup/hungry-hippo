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

  private var crawlDirectoryRouter: Option[ActorRef] = None
  private var loadCoordinateRouter: Option[ActorRef] = None
  private var parseMavenMetadataRouter: Option[ActorRef] = None

  override def receive = {
    case StartCrawling if (state == Stopped) =>
      state = Started
      notificationActor ! CrawlStateChanged(state)

      // Create actors for CrawlDirectoryActor, LoadCoordianteActor, ParseMavenMetadataActor
      crawlDirectoryRouter = Some(context.actorOf(Props[CrawlDirectoryActor].withRouter(
        RoundRobinRouter(nrOfInstances = 5)), "crawlDirectory"))
      loadCoordinateRouter = Some(Akka.system.actorOf(Props[LoadCoordinateActor].withRouter(
        RoundRobinRouter(nrOfInstances = 5)), "loadCoordinate"))
      parseMavenMetadataRouter = Some(Akka.system.actorOf(Props[ParseMavenMetadataActor].withRouter(
        RoundRobinRouter(nrOfInstances = 5)), "parseMavenMetadata"))

      val maybeDirs: Option[List[String]] = Play.current.configuration.getStringList("crawl.roots").map(_.toList)
      maybeDirs foreach { dirs: List[String] =>
          dirs foreach { dir: String =>
            crawlDirectoryRouter.foreach(_ ! CrawlDirectory(dir, ""))
          }
      }

    case StopCrawling if (state == Started) =>
      state = Stopped
      notificationActor ! CrawlStateChanged(state)
      killActors()
    case GetCrawlStatus =>
      sender ! state
    case _ =>
      context.system.log.info("do what now?")
  }

  private def killActors() {
    crawlDirectoryRouter.foreach(_ ! PoisonPill)
    loadCoordinateRouter.foreach(_ !  PoisonPill)
    parseMavenMetadataRouter.foreach(_ ! PoisonPill)
    crawlDirectoryRouter = None
    loadCoordinateRouter = None
    parseMavenMetadataRouter = None
  }
}

