package com.javaposse.hungryhippo.actors

import akka.actor.Actor
import akka.actor.ActorRef
import com.javaposse.hungryhippo.event.Events.CrawlStateChanged
import play.api.libs.iteratee.Concurrent
import play.api.libs.json._


sealed abstract class StatusListeningActorMessage
case object DisconnectChannel extends StatusListeningActorMessage

class WebSocketActor(notificationActor: ActorRef, channel: Concurrent.Channel[JsValue]) extends Actor {
  notificationActor ! Watch(Set(com.javaposse.hungryhippo.event.Events.CrawlStateChange))

  def receive = {
    case DisconnectChannel =>
      notificationActor ! Unwatch(Set(com.javaposse.hungryhippo.event.Events.CrawlStateChange))
      context.stop(self)
    case stateChange: CrawlStateChanged =>
      val json = Map("state" -> stateChange.crawlState.toString)
      val out: JsValue = Json.toJson(json)
      channel.push(out)
    case _ =>
  }
}
