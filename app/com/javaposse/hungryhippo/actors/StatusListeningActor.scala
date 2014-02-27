package com.javaposse.hungryhippo.actors

import akka.actor.Actor
import akka.actor.ActorRef
import play.api.libs.iteratee.Concurrent
import play.api.libs.json._


sealed abstract class StatusListeningActorMessage
case object DisconnectChannel extends StatusListeningActorMessage
case class StateChanged(state: ControllerState) extends StatusListeningActorMessage

class StatusListeningActor(crawlControlActor: ActorRef, channel: Concurrent.Channel[JsValue]) extends Actor {
  crawlControlActor ! WatchCrawlState

  def receive = {
    case DisconnectChannel =>
      crawlControlActor ! UnwatchCrawlState
      // TODO: kill myself now?!?
    case stateChange: StateChanged =>
      val json = Map("state" -> stateChange.state.toString)
      val out: JsValue = Json.toJson(json)
      channel.push(out)

  }
}
