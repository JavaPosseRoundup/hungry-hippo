package controllers

import play.api.mvc.{Action, Controller}
import akka.actor.{Props, ActorRef}
import play.api.libs.concurrent.Akka
import com.javaposse.hungryhippo.actors.{StopCrawling, StartCrawling, CrawlControlActor}
import play.api.Play.current

object Application extends Controller {
  def index = Action {
    Ok(views.html.index("Hello Play Framework"))
  }

  def startCrawler = Action {
    val crawlControlActor: ActorRef = Akka.system.actorOf(Props[CrawlControlActor])
    crawlControlActor ! StartCrawling
    Ok("started")
  }

  def stopCrawler = Action {
    val crawlControlActor: ActorRef = Akka.system.actorOf(Props[CrawlControlActor])
    crawlControlActor ! StopCrawling
    Ok("stopped")
  }

}