package com.javaposse.hungryhippo.actors

import akka.actor.{PoisonPill, Props, ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, DefaultTimeout, TestKit}
import org.scalatest.{BeforeAndAfterEach, BeforeAndAfterAll, Matchers, WordSpecLike}
import com.javaposse.hungryhippo.event.Events.{CrawlStateChanged, EventType, CrawlStateChange}
import scala.collection.mutable

class NotificationActorTest extends TestKit(ActorSystem("test")) with DefaultTimeout with ImplicitSender
with WordSpecLike with Matchers with BeforeAndAfterAll with BeforeAndAfterEach {
  private var notificationActor: Option[ActorRef] = None

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

  override def beforeEach() {
    notificationActor = Some(system.actorOf(Props[NotificationActor]))
  }

  override def afterEach() {
    notificationActor.foreach(_ ! PoisonPill)
    notificationActor = None
  }

  "a NotificationActor" should {

    "remember subscription" in {
      notificationActor.foreach(_ ! Watch(Set(CrawlStateChange)))
      expectNoMsg
      notificationActor.foreach(_ ! Subscriptions)
      expectMsg(Some(mutable.Set[EventType](CrawlStateChange)))
    }

    "be notified when subscribed" in {
      notificationActor.foreach(_ ! Watch(Set(CrawlStateChange)))
      expectNoMsg
      notificationActor.foreach(_ ! CrawlStateChanged(Started))
      expectMsg(CrawlStateChanged(Started))
    }
  }
}
