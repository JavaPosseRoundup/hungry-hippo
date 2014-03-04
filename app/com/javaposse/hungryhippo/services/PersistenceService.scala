package com.javaposse.hungryhippo.services

import com.javaposse.hungryhippo.models.{Coordinate, Module}
import scala.concurrent.Future

trait PersistenceService {
    def saveOrUpdateModule(module: Module): Future[Unit]

    def findModule(coordinate: Coordinate) : Future[Option[Module]]

    def removeModule(coordinate: Coordinate): Future[Unit]
}


