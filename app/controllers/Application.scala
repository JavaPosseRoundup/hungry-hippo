package controllers

import com.javaposse.hungryhippo.actors.{CrawlState, ActorRefs, StopCrawling, StartCrawling}
import play.api.mvc._
import akka.util.Timeout
import akka.pattern.ask
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.mvc._
import com.javaposse.hungryhippo.actors.ControllerState
import play.api._
import play.api.Play.current


object Application extends Controller {
  implicit val askTimeout = Timeout(2.seconds)

  import ActorRefs._

  def index = Action {
    Ok(views.html.index("Hello Play Framework"))
  }

  def crawlerStatus = Action.async {
    val stateResponse = crawlControlActor ? CrawlState
    stateResponse.map {
      case status: ControllerState =>
        Ok(status.toString)
      case _ =>
        InternalServerError("Unknown Error")
    } recover {
      case e: Exception =>
        InternalServerError(e.toString)
    }
  }


  def startCrawler = Action {
    crawlControlActor ! StartCrawling
    Ok("started")
  }

  def stopCrawler = Action {
    crawlControlActor ! StopCrawling
    Ok("stopped")
  }

}