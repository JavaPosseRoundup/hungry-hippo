package com.javaposse.hungryhippo.services.mongo

import com.javaposse.hungryhippo.services.PersistenceService
import com.javaposse.hungryhippo.models.{Module, Coordinate}
import scala.concurrent.Future
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.bson.{BSONDocumentWriter, Macros, BSONDocument}
import reactivemongo.core.commands.{GetLastError, LastError}
import reactivemongo.bson.DefaultBSONHandlers.BSONDocumentIdentity

trait MongoParams {
    def nodes : Seq[String]
    def dbName: String
}

/**
 * Created by dmitry on 2/26/14.
 */
trait MongoPersistenceService extends PersistenceService{
    self: MongoParams =>

    import reactivemongo.api._
    import scala.concurrent.ExecutionContext.Implicits.global

    implicit val coordinateBson = Macros.handler[Coordinate]
    implicit val moduleBson = Macros.handler[Module]




    val driver = new MongoDriver
    val connection = driver.connection(nodes)
    val db = connection.db(dbName)

    def collection = db.collection[BSONCollection]("modules")

    def saveOrUpdateModule(module: Module): Future[Unit] = {

        collection.update(selectByCoordinate(module.id), module, GetLastError(),
                upsert = true, multi = false).map(_=> ())
        
    }



    def findModule(coordinate: Coordinate): Future[Option[Module]] = {
        collection.find(selectByCoordinate(coordinate)).one[Module]
    }

    def removeModule(coordinate: Coordinate): Future[Unit] = {
        collection.remove(selectByCoordinate(coordinate)).map(_=> ())
    }

    def selectByCoordinate(coordinate: Coordinate): BSONDocument = {
        BSONDocument("id.groupId" -> coordinate.groupId,
            "id.artifactId" -> coordinate.artifactId,
            "id.version" -> coordinate.version
        )
    }
}
