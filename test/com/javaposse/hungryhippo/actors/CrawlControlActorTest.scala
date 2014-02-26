package com.javaposse.hungryhippo.actors

import org.scalatest.{BeforeAndAfterAll, Matchers, FunSuiteLike, FunSuite}
import akka.testkit.{ImplicitSender, TestKit}
import akka.actor.{Props, ActorSystem}
import scala.concurrent.duration._
import java.util.concurrent.TimeUnit
import scala.util.Success
import com.javaposse.hungryhippo.models.{Coordinate, Module}
import akka.pattern.ask
import akka.util.Timeout

/**
 * Created by cmarks on 2/26/14.
 */
class CrawlControlActorTest extends  TestKit(ActorSystem("test")) with FunSuiteLike with Matchers
with BeforeAndAfterAll with ImplicitSender{

  override def afterAll()
  {
    TestKit.shutdownActorSystem(system)
  }

  implicit val askTimeout = Timeout(15.seconds)

  test("start should indicated started") {

    val actorRef = system.actorOf(Props[CrawlControlActor])

    actorRef ! StartCrawling

    actorRef ! CrawlState
    expectMsg(Started)

  }
}
