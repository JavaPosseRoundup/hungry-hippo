package com.javaposse.hungryhippo.actors

import com.javaposse.hungryhippo.models.Coordinate
import akka.actor.Actor
import play.api.libs.ws.WS.WSRequestHolder
import play.api.libs.ws.{Response, WS}
import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global

/**
 * Take a coordinate, and download the pom to pass on to pom parser
 */
class LoadCoordinateActor extends Actor {
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
              response.xml
              // Send to ProcessPom
              //context.actorSelection("/user/parseMavenMetadata") ! ParseMavenMetadata(d, response.xml)
            }
          }
        // TODO record a missing pom
      } // TODO record an IO Exception

    }
  }

  def toPath(coord: Coordinate) = {
    val groupLoc = coord.groupId.replaceAllLiterally(".", "/")
    val artifactLoc = coord.artifactId.replaceAllLiterally(".", "/")
    val versionLoc = coord.version
    s"${groupLoc}/${artifactLoc}/${versionLoc}"
  }
}

case class LoadCoordinate(dir: CrawlDirectory, coord: Coordinate)
