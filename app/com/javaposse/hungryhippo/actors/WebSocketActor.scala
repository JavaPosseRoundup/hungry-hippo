package com.javaposse.hungryhippo.actors

import akka.actor.Actor
import akka.actor.ActorRef
import com.javaposse.hungryhippo.event.Events._
import play.api.libs.iteratee.Concurrent
import play.api.libs.json._


sealed abstract class WebSocketActorMessage
case object DisconnectChannel extends WebSocketActorMessage

object WebSocketActor {
  val EventsToWatch = Set[EventType](CrawlStateChange, DirectoryCrawl)
}

class WebSocketActor(notificationActor: ActorRef, channel: Concurrent.Channel[JsValue]) extends Actor {

  import WebSocketActor._

  notificationActor ! Watch(EventsToWatch)

  def receive = {
    case DisconnectChannel =>
      notificationActor ! Unwatch(EventsToWatch)
      context.stop(self)
    case stateChange: CrawlStateChanged =>
      // TODO: make constants for keys
      val json = Map("crawlState" -> stateChange.crawlState.toString,
        "eventType" -> stateChange.eventType.wireName)
      val out: JsValue = Json.toJson(json)
      channel.push(out)
    case directoryCrawled: DirectoryCrawled =>
      val json = Map("eventType" -> directoryCrawled.eventType.wireName,
        "dir" -> directoryCrawled.crawlDirectory.uri)
        val out: JsValue = Json.toJson(json)
        channel.push(out)
    case _ =>
  }
}
