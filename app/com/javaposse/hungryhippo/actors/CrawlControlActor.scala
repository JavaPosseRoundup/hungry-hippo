package com.javaposse.hungryhippo.actors

import akka.actor.{ActorRef, Actor}

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
  private val watchers: scala.collection.mutable.Set[ActorRef] = scala.collection.mutable.HashSet()

  override def receive = {
    case CrawlState =>
      sender ! state
    case StartCrawling if (state == Stopped) =>
      state = Started

    case StopCrawling if (state == Started) =>
      state = Stopped
    case WatchCrawlState =>
      watchers += sender
    case UnwatchCrawlState =>
      watchers -= sender
    case _ =>
      context.system.log.info("do what now?")
  }

  def updateWatchers = {
    watchers.foreach{ _ ! state}
  }
}

