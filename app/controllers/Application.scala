package controllers

import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import com.javaposse.hungryhippo.actors._
import play.Logger
import play.api.Routes
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.iteratee.{Concurrent, Iteratee}
import play.api.libs.json._
import play.api.mvc._
import play.libs.Akka
import scala.concurrent.duration._
import scala.concurrent.Future

object Application extends Controller {
  implicit val askTimeout = Timeout(2.seconds)

  import ActorRefs._

  def index = Action {
    Ok(views.html.index())
  }

  def crawlerStatus = Action.async {
    val stateResponse = crawlControlActor ? GetCrawlStatus
    stateResponse.map {
      case status: CrawlState =>
        val json = Map("crawlState" -> status.toString)
        val out: JsValue = Json.toJson(json)

        Ok(out.toString)
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

  def crawlerStatusWs = WebSocket.async[JsValue] { request =>
    val (out, channel) = Concurrent.broadcast[JsValue]
    val listeningActor = Akka.system.actorOf(Props(classOf[WebSocketActor], ActorRefs.notificationActor, channel))

    val in = Iteratee.foreach[JsValue] { message =>
      Logger.info(s"Received WebSocket message: $message")
    } map { _ =>
      Logger.info("User Disconnected from WebSocket")
      listeningActor ! DisconnectChannel
    }

    // TODO: this seems kinda weird.
    Future.successful((in, out))
  }

  def javascriptRoutes = Action {
    implicit request =>
      Ok(
        Routes.javascriptRouter("jsRoutes")(
          routes.javascript.Application.crawlerStatusWs,
          routes.javascript.Application.crawlerStatus,
          routes.javascript.Application.startCrawler,
          routes.javascript.Application.stopCrawler
        )
      ).as("text/javascript")
  }

}


