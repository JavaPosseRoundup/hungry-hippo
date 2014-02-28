package com.javaposse.hungryhippo.actors

import akka.actor.{ActorRef, Actor}
import com.javaposse.hungryhippo.event.Events.{Event, EventType}
import scala.collection.mutable

sealed abstract class NotificationMessage
case class Watch(eventTypes: Set[EventType]) extends NotificationMessage
case class Unwatch(eventTypes: Set[EventType]) extends NotificationMessage
case object Subscriptions extends NotificationMessage

class NotificationActor extends Actor {
  private val watchers = new mutable.HashMap[ActorRef, mutable.Set[EventType]] with mutable.MultiMap[ActorRef, EventType]
  private val events = new mutable.HashMap[EventType, mutable.Set[ActorRef]] with mutable.MultiMap[EventType, ActorRef]
  
  override def receive = {
    case watchMsg: Watch =>
      watch(watchMsg.eventTypes, sender)
    case unwatchMsg: Unwatch =>
      unwatch(unwatchMsg.eventTypes, sender)
    case event: Event =>
      notify(event)
    case Subscriptions =>
      sender ! watchers.get(sender)
    case _ =>
  }

  private def watch(eventTypes: Set[EventType], actor: ActorRef) {
    eventTypes.foreach { eventType: EventType =>
      watchers.addBinding(actor, eventType)
      events.addBinding(eventType, actor)
    }

  }

  private def unwatch(eventTypes: Set[EventType], actor: ActorRef) {
    eventTypes.foreach{ eventType: EventType =>
      watchers.removeBinding(actor, eventType)
      events.removeBinding(eventType, actor)
    }
  }

  private def notify(event: Event) {
    events.get(event.eventType) foreach { actors =>
      actors foreach {
        _ ! event
      }
    }
  }

}
