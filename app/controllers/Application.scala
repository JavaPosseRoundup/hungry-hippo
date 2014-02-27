package controllers

import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import com.javaposse.hungryhippo.actors._
import play.api.Routes
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.iteratee.{Enumerator, Iteratee}
import play.api.libs.json._
import play.api.mvc.Results._
import play.api.mvc._
import play.libs.Akka
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Random

object Application extends Controller with Secured {
  implicit val askTimeout = Timeout(2.seconds)
  val timerActor = Akka.system.actorOf(Props[TimerActor])

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

  def crawlerStatusWs = WebSocket.using[String] { request =>
    crawlControlActor ! WatchCrawlState

    // TODO: how do I get told when the status changes??

  // Just consume and ignore the input
    val in = Iteratee.consume[String]()

    // Send a single 'Hello!' message and close
    val out = Enumerator("Hello!").andThen(Enumerator.eof)

    (in, out)
  }

  def start = withAuth {
    userId => implicit request =>
      timerActor ! Start(userId)
      Ok("")
  }


  def stop = withAuth {
    userId => implicit request =>
      timerActor ! Stop(userId)
      Ok("")
  }


  /**
   * This function crate a WebSocket using the
   * enumertator linked to the current user,
   * retreived from the TaskActor.
   */
  def indexWS = withAuthWS {
    userId =>

//      implicit val timeout = Timeout(3 seconds)

      // using the ask pattern of akka,
      // get the enumerator for that user
      (timerActor ? StartSocket(userId)) map {
        enumerator =>

        // create a Itreatee which ignore the input and
        // and send a SocketClosed message to the actor when
        // connection is closed from the client
          (Iteratee.ignore[JsValue] mapDone {
            _ =>
              timerActor ! SocketClosed(userId)
          }, enumerator.asInstanceOf[Enumerator[JsValue]])
      }
  }

  def javascriptRoutes = Action {
    implicit request =>
      Ok(
        Routes.javascriptRouter("jsRoutes")(
          routes.javascript.Application.indexWS,
          routes.javascript.Application.crawlerStatusWs,
          routes.javascript.Application.start,
          routes.javascript.Application.stop
        )
      ).as("text/javascript")
  }


}

trait Secured {
  def username(request: RequestHeader) = {
    //verify or create session, this should be a real login
    request.session.get(Security.username)
  }

  /**
   * When user not have a session, this function create a
   * random userId and reload index page
   */
  def unauthF(request: RequestHeader) = {
    val newId: String = new Random().nextInt().toString()
    Redirect(routes.Application.index).withSession(Security.username -> newId)
  }

  /**
   * Basi authentication system
   * try to retieve the username, call f() if it is present,
   * or unauthF() otherwise
   */
  def withAuth(f: => Int => Request[_ >: AnyContent] => Result): EssentialAction = {
    Security.Authenticated(username, unauthF) {
      username =>
        Action(request => f(username.toInt)(request))
    }
  }

  /**
   * This function provide a basic authentication for
   * WebSocket, lekely withAuth function try to retrieve the
   * the username form the session, and call f() funcion if find it,
   * or create an error Future[(Iteratee[JsValue, Unit], Enumerator[JsValue])])
   * if username is none
   */
  def withAuthWS(f: => Int => Future[(Iteratee[JsValue, Unit], Enumerator[JsValue])]): WebSocket[JsValue] = {

    // this function create an error Future[(Iteratee[JsValue, Unit], Enumerator[JsValue])])
    // the itaratee ignore the input and do nothing,
    // and the enumerator just send a 'not authorized message'
    // and close the socket, sending Enumerator.eof
    def errorFuture = {
      // Just consume and ignore the input
      val in = Iteratee.ignore[JsValue]

      // Send a single 'Hello!' message and close
      val out = Enumerator(Json.toJson("not authorized")).andThen(Enumerator.eof)

      Future {
        (in, out)
      }
    }

    WebSocket.async[JsValue] {
      request =>
        username(request) match {
          case None =>
            errorFuture

          case Some(id) =>
            f(id.toInt)

        }
    }
  }
}


