package com.javaposse.hungryhippo.actors

import akka.actor.Actor

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
    case StartCrawling if (state == Stopped) =>
      state = Started

    case StopCrawling if (state == Started) =>
      state = Stopped
    case _ =>
      context.system.log.info("do what now?")
  }
}

