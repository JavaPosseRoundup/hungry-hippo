package com.javaposse.hungryhippo.actors

import akka.testkit.{ImplicitSender, TestKit}
import akka.actor.ActorSystem
import org.scalatest._
import scala.util.{Failure, Success}
import com.javaposse.hungryhippo.models.{Coordinate, Module}
import scala.concurrent.duration.FiniteDuration
import java.util.concurrent.TimeUnit
import com.javaposse.hungryhippo.models.Module
import com.javaposse.hungryhippo.models.Coordinate

/**
 * Created by dmitry on 2/26/14.
 */
class PomParserActorSpec extends  TestKit(ActorSystem("test")) with FunSuiteLike with Matchers
        with BeforeAndAfterAll with ImplicitSender
{
    val actor = system.actorOf(PomParserActor.props)

    val pomText =
        """|<project>
          |  <modelVersion>4.0.0</modelVersion>
          |  <groupId>com.mycompany.app</groupId>
          |  <artifactId>my-app</artifactId>
          |  <version>1</version>
          |
          |  <dependencies>
          |    <dependency>
          |      <groupId>org.apache.maven</groupId>
          |      <artifactId>maven-artifact</artifactId>
          |      <version>3.0</version>
          |    </dependency>
          |    <dependency>
          |      <groupId>org.apache.maven</groupId>
          |      <artifactId>maven-project</artifactId>
          |
          |    </dependency>
          |  </dependencies>
          |
          |</project>
          |
        """.stripMargin

    val url = "http://foo.bar"

    override def afterAll()
    {
        TestKit.shutdownActorSystem(system)
    }

    test("pom parser actor should parse valid pom") {

            actor ! PomParserActor.ParsePom(url, pomText)

            expectMsg(FiniteDuration(1l, TimeUnit.SECONDS), PomParserActor.ParsePomResponse(Success(
                Module(Coordinate("com.mycompany.app", "my-app", "1"), url,
                    Seq(Coordinate("org.apache.maven", "maven-artifact", "3.0"),
                    Coordinate("org.apache.maven", "maven-project", "")) ))))
    }

    test("pom parser actor should handle invalid XML") {

        actor ! PomParserActor.ParsePom(url, "foo")

        val msg = receiveOne(FiniteDuration(1l, TimeUnit.SECONDS))

        msg match {
            case PomParserActor.ParsePomResponse(Failure(e)) => e.printStackTrace()
            case PomParserActor.ParsePomResponse(r) => fail(s"result should be a failure, but was $r")

        }

    }


}
