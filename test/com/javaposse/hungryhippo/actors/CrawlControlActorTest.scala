package com.javaposse.hungryhippo.actors

import akka.actor.{PoisonPill, ActorRef, Props, ActorSystem}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import org.scalatest._

class CrawlControlActorTest extends TestKit(ActorSystem("test")) with DefaultTimeout with ImplicitSender
with WordSpecLike with Matchers with BeforeAndAfterAll with BeforeAndAfterEach  {
  lazy val notificationActor: ActorRef = system.actorOf(Props[NotificationActor])
  private var crawlControlActor: Option[ActorRef] = None

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

  override def beforeEach() {
    crawlControlActor = Some(system.actorOf(Props(classOf[CrawlControlActor], notificationActor,
      Some(List("http://dl.bintray.com/nebula/gradle-plugins/")))))
  }

  override def afterEach() {
    crawlControlActor.foreach(_ ! PoisonPill)
    crawlControlActor = None
  }

  "a CrawlControlActor" should {

    "indicate started when started" in {
        crawlControlActor.foreach{ actorRef =>
          actorRef ! StartCrawling
          expectNoMsg
          actorRef ! GetCrawlStatus
          expectMsg(Started)
      }

    }

    "have state stopped by default" in  {
      crawlControlActor.foreach(_ ! GetCrawlStatus)
      expectMsg(Stopped)
    }

    "indicate proper states after restart" in {
      crawlControlActor.foreach{ actorRef =>
        actorRef ! StartCrawling
        expectNoMsg
        actorRef ! GetCrawlStatus
        expectMsg(Started)
        actorRef ! StopCrawling
        expectNoMsg
        actorRef ! GetCrawlStatus
        expectMsg(Stopped)
      }
    }
  }
}
