package com.javaposse.hungryhippo.actors

import scala.xml.NodeSeq
import akka.actor.Actor
import com.javaposse.hungryhippo.models.Coordinate

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
  override def receive = {
    case p: ParseMavenMetadata => {
      context.system.log.info(s"parsing metadata ${p.dir.uri}")
      val groupId = (p.document \ "groupId").map(_.text).head
      val artifactId = (p.document \ "artifactId").map(_.text).head

      val versionElements : NodeSeq = p.document \ "versioning" \ "versions" \ "version"
      val versions: Seq[String] = versionElements.map(_.text)

      val coordinates = versions.map( Coordinate(groupId, artifactId, _) )
      coordinates.map(LoadCoordinate(p.dir, _)).foreach {
        context.actorSelection("/user/loadCoordinate") ! _
      }
    }
  }
}

case class ParseMavenMetadata( dir: CrawlDirectory, document: NodeSeq )