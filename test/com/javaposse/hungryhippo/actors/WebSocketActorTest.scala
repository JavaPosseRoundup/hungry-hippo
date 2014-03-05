package com.javaposse.hungryhippo.actors

import akka.actor.{Props, ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, DefaultTimeout, TestKit}
import com.javaposse.hungryhippo.TestChannel
import com.javaposse.hungryhippo.event.Events.{DirectoryCrawl, DirectoryCrawled, CrawlStateChange, CrawlStateChanged}
import org.scalatest.{BeforeAndAfterEach, BeforeAndAfterAll, Matchers, WordSpecLike}
import play.api.libs.json.{Json, JsValue}

class WebSocketActorTest extends TestKit(ActorSystem("test")) with DefaultTimeout with ImplicitSender
with WordSpecLike with Matchers with BeforeAndAfterAll with BeforeAndAfterEach {

  private var channel = new TestChannel[JsValue]
  private lazy val notificationActor: ActorRef = system.actorOf(Props[NotificationActor])
  private var webSocketActor: Option[ActorRef] = None

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

  override def beforeEach() {
    channel = new TestChannel[JsValue]
    webSocketActor = Some(system.actorOf(Props(classOf[WebSocketActor], notificationActor, channel)))
    webSocketActor should not be (None)
  }

  override def afterEach() {
    webSocketActor.foreach(_ ! DisconnectChannel)
  }

  "a WebSocketActor" should {

    "push CrawlStateChange started to channel" in {
      webSocketActor.foreach{ actorRef =>
        actorRef ! CrawlStateChanged(Started)
        expectNoMsg
        val json = Map("crawlState" -> Started.toString,
          "eventType" -> CrawlStateChange.wireName)
        expectChunk(channel, json)
      }
    }

    "push CrawlStateChange stopped to channel" in {
      webSocketActor.foreach{ actorRef =>
        actorRef ! CrawlStateChanged(Stopped)
        expectNoMsg
        val json = Map("crawlState" -> Stopped.toString,
          "eventType" -> CrawlStateChange.wireName)
        expectChunk(channel, json)
      }
    }

    "push DirectoryCrawled to channel" in {
      webSocketActor.foreach{ actorRef =>
        val crawlDirectory = CrawlDirectory("http://fakerepo.org", "/my/awesome/repo")
        actorRef ! DirectoryCrawled(crawlDirectory)
        expectNoMsg
        val json = Map("eventType" -> DirectoryCrawl.wireName,
          "dir" -> crawlDirectory.uri)
        expectChunk(channel, json)
      }
    }


  }

  private def expectChunk(channel: TestChannel[JsValue], expectedData: Map[String,String]) {
    val out: JsValue = Json.toJson(expectedData)
    channel.last.map{jsVal =>
      jsVal should be (out)
    }
  }

}
