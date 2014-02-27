package com.javaposse.hungryhippo.services;

import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import com.javaposse.hungryhippo.models.{Coordinate, Module}

/**
 * Created by dmitry on 2/26/14.
 */
@RunWith(classOf[JUnitRunner])
class PomParserSpec extends Specification {

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

    "pom parser" should {
        "parse correctly" in {

            val url = "http://foo.bar"

            val pom = new PomParser().parsePom( url, pomText )

            pom.repoUrl ==== url and
            pom.id ==== Coordinate("com.mycompany.app", "my-app", "1") and
            pom.dependencies ==== Seq(
               Coordinate("org.apache.maven", "maven-artifact", "3.0"),
               Coordinate("org.apache.maven", "maven-project", "")
            )


        }
    }
}
