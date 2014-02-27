package com.javaposse.hungryhippo.services.mongo

import org.specs2.mutable.Specification
import com.javaposse.hungryhippo.models.{Coordinate, Module}
import scala.concurrent.Await
import scala.concurrent.duration.{FiniteDuration, Duration}
import java.util.concurrent.TimeUnit

/**
 * Created by dmitry on 2/26/14.
 */
class MongoPersistenceServiceTest extends Specification {

    val timeout: FiniteDuration = Duration(10l, TimeUnit.SECONDS)

    val module: Module = Module(Coordinate("com.mycompany.app", "my-app", "1"), "http://foo.com",
        Seq(Coordinate("org.apache.maven", "maven-artifact", "3.0"),
            Coordinate("org.apache.maven", "maven-project", "")))


    "The service" should {
        "insert module if doesn't exit and update if exists" in {

            val service = new Object with MongoPersistenceService with MongoParams {
                def nodes: Seq[String] = Seq("localhost")

                def dbName: String = "test"
            }


            Await.result(service.removeModule(module.id), timeout)


            Await.result(service.saveOrUpdateModule(module), timeout)

            val found1 = Await.result(service.findModule(module.id), timeout)

            val modifiedModule = module.copy(repoUrl = module.repoUrl+"/foo")

            Await.result(service.saveOrUpdateModule(modifiedModule), timeout)

            val found2 = Await.result(service.findModule(module.id), timeout)

            found1 === Some(module) &&
            found2 === Some(modifiedModule)

        }
    }


}
