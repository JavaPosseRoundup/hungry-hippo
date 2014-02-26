package com.javaposse.hungryhippo.actors

import akka.actor.Actor

class CrawlControlActor extends Actor {
  override def receive = {
    case StartCrawling =>
      context.system.log.info("started crawling")
    case StopCrawling =>
      context.system.log.info("stopped crawling")
    case _ =>
      context.system.log.info("do what now?")
  }
}


case object StartCrawling

case object StopCrawling
