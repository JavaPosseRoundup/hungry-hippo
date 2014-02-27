package com.javaposse.hungryhippo.actors

import akka.actor.Actor
import play.api.libs.ws._
import scala.concurrent.Future
import play.api.libs.ws.WS.WSRequestHolder
import com.javaposse.hungryhippo.directory.SubdirectoryProvider

/**
 * First look at maven-metadata.xml, then fallback to a directory listing
 */
class CrawlDirectoryActor extends Actor {

  implicit val context2 = scala.concurrent.ExecutionContext.Implicits.global

  override def receive = {
    case d: CrawlDirectory =>
      context.system.log.info(s"started crawling ${d.uri}")
      val metadataUri = s"${d.uri}/maven-metadata.xml"
      val holder : WSRequestHolder = WS.url(metadataUri).withFollowRedirects(true).withRequestTimeout(10000)
      val futureResponse : Future[Response] = holder.get()
      futureResponse onSuccess {
        case response: Response =>
          response.status match {
            case x if 200 until 300 contains x => {
              // Send to ParseMavenMetadataActor
              context.actorSelection("/user/parseMavenMetadata") ! ParseMavenMetadata(d, response.xml)
            }
            case 404 => {
                fallbackToHtml(d)
            }
            case _ => recordUnknownStatus(d, response)
          }

      } // TODO record an IO Exception

    case _ =>
      context.system.log.info("do what now?")
  }

  def recordUnknownStatus(dir: CrawlDirectory, response: Response) = {
    context.system.log.warning(s"Unable to crawl ${dir.uri}, got ${response.statusText}")
  }

  def fallbackToHtml(d: CrawlDirectory) {
    val directoryUri = s"${d.uri}/"
    val holder : WSRequestHolder = WS.url(directoryUri).withFollowRedirects(true).withRequestTimeout(10000)
    val futureResponse : Future[Response] = holder.get()
    futureResponse onSuccess {
      case response: Response =>
        response.status match {
          case x if 200 until 300 contains x => {
            SubdirectoryProvider.findDirectories(response.body).map(d.subdirectory(_)).foreach {
              context.actorSelection("/user/crawlDirectory") ! _
            }
          }
          case _ => recordUnknownStatus(d, response)
        }

    } // TODO record an IO Exception

  }
}

case class CrawlDirectory( base: String, path: String ) {
  def uri: String = {
    s"${base}/${path}"
  }

  def subdirectory(subdir:String ): CrawlDirectory = {
      CrawlDirectory(base, path + "/" + subdir)
  }
}

