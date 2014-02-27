package com.javaposse.hungryhippo.actors

import org.scalatest.{BeforeAndAfterAll, Matchers, FunSuiteLike}
import akka.testkit.{ImplicitSender, TestKit}
import akka.actor.{Props, ActorSystem}
import scala.concurrent.duration._
import akka.util.Timeout

/**
 * Created by cmarks on 2/26/14.
 */
class CrawlControlActorTest extends TestKit(ActorSystem("test")) with FunSuiteLike with Matchers
with BeforeAndAfterAll with ImplicitSender {

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

  implicit val askTimeout = Timeout(15.seconds)



  test("start should indicated started") {
    val actorRef = system.actorOf(Props[CrawlControlActor])
    actorRef ! StartCrawling

    actorRef ! CrawlState
    expectMsg(Started)

  }

  test("default status should be stopped") {
    val actorRef = system.actorOf(Props[CrawlControlActor])
    actorRef ! CrawlState
    expectMsg(Stopped)
  }

  test("start then stop should indicate proper states") {
    val actorRef = system.actorOf(Props[CrawlControlActor])
    actorRef ! StartCrawling
    actorRef ! CrawlState
    expectMsg(Started)

    actorRef ! StopCrawling
    actorRef ! CrawlState
    expectMsg(Stopped)
  }
}
