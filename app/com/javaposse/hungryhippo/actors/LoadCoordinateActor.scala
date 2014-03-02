package com.javaposse.hungryhippo.actors

import com.javaposse.hungryhippo.models.Coordinate
import akka.actor.{Props, Actor}
import play.api.libs.ws.WS.WSRequestHolder
import play.api.libs.ws.{Response, WS}
import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global
import akka.pattern.ask
import akka.pattern.pipe
import akka.util.Timeout
import java.util.concurrent.TimeUnit
import akka.routing.RoundRobinRouter

/**
 * Take a coordinate, and download the pom to pass on to pom parser
 */
class LoadCoordinateActor extends Actor {
  implicit val timeout = Timeout(2, TimeUnit.MINUTES)
  private lazy val pomParserActor = context.actorOf(Props[PomParserActor].withRouter(
    RoundRobinRouter(nrOfInstances = 15)), "pomParser")

  override def receive = {
    case p: LoadCoordinate => {
      val uri = s"${p.dir.base}/${toPath(p.coord)}"
      context.system.log.info(s"crawling coordinate ${p.coord} at ${uri}")
      val holder : WSRequestHolder = WS.url(uri).withFollowRedirects(true).withRequestTimeout(10000)
      val futureResponse : Future[Response] = holder.get()
      futureResponse onSuccess {
        case response: Response =>
          response.status match {
            case x if 200 until 300 contains x => {
              (pomParserActor ? PomParserActor.ParsePom(p.dir.base, response.body)).map {
                case PomParserActor.ParsePomResponse(m)=> {
                  context.system.log.info(s"Persisting ${m.get.id}")
                  m
                }
              }
              // .pipeTo() to actorRef of persistence
            }
          }
        // TODO record a missing pom
      } // TODO record an IO Exception

    }
  }

  def toPath(coord: Coordinate) = {
    val groupLoc = coord.groupId.replaceAllLiterally(".", "/")
    val artifactLoc = coord.artifactId
    val versionLoc = coord.version
    s"${groupLoc}/${artifactLoc}/${versionLoc}/${artifactLoc}-${versionLoc}.pom"
  }
}

case class LoadCoordinate(dir: CrawlDirectory, coord: Coordinate)
