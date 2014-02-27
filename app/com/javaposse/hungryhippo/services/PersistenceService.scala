package com.javaposse.hungryhippo.services

import com.javaposse.hungryhippo.models.{Coordinate, Module}
import scala.concurrent.Future

/**
 * Created by dmitry on 2/26/14.
 */
trait PersistenceService
{
    def saveOrUpdateModule(modlue: Module): Future[Unit]

    def findModule(coordinate: Coordinate) : Future[Option[Module]]
}


