package com.javaposse.hungryhippo.actors

import akka.actor.{Props, Actor}
import akka.routing.RoundRobinRouter
import com.javaposse.hungryhippo.models.Coordinate
import scala.xml.NodeSeq

/**
 * Given a maven-metadata.xml, we create Coordinate that need to be downloaded by the loadCoordinateActor
 *
 * E.g. http://repo.maven.apache.org/maven2/com/netflix/archaius/archaius-aws/maven-metadata.xml
 *
 * <metadata>
 * <groupId>com.netflix.archaius</groupId>
 * <artifactId>archaius-aws</artifactId>
 * <versioning>
 *   <latest>0.6.0</latest>
 *   <release>0.6.0</release>
 *   <versions>
 *     <version>0.5.16</version>
 *     <version>0.6.0</version>
 *   </versions>
 *   <lastUpdated>20140221193355</lastUpdated>
 * </versioning>
 * </metadata>
 */
class ParseMavenMetadataActor extends Actor {
  lazy val loadCoordinateRouter = context.actorOf(Props[LoadCoordinateActor].withRouter(
    RoundRobinRouter(nrOfInstances = 5)), "loadCoordinate")

  override def receive = {
    case p: ParseMavenMetadata => {
      context.system.log.info(s"parsing metadata ${p.dir.uri}")
      val groupId = (p.document \ "groupId").map(_.text).head
      val artifactId = (p.document \ "artifactId").map(_.text).head

      val versionElements : NodeSeq = p.document \ "versioning" \ "versions" \ "version"
      val versions: Seq[String] = versionElements.map(_.text)

      val coordinates = versions.map( Coordinate(groupId, artifactId, _) )
      coordinates.map(LoadCoordinate(p.dir, _)).foreach {
        loadCoordinateRouter ! _
      }
    }
  }
}

case class ParseMavenMetadata( dir: CrawlDirectory, document: NodeSeq )