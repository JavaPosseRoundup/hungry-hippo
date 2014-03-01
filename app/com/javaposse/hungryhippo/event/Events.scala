package com.javaposse.hungryhippo.event

import com.javaposse.hungryhippo.actors.{CrawlDirectory, CrawlState}

object Events {
  val AllEventTypes = Set(CrawlStateChange)

  trait Event {
    def eventType: EventType
  }

  sealed abstract class EventType(val wireName: String)
  
  case object CrawlStateChange extends EventType("CrawlStateChanged")
  case object DirectoryCrawl extends EventType("CrawlDir")
  
  case class CrawlStateChanged(crawlState: CrawlState) extends Event {
    def eventType = CrawlStateChange
  }

  case class DirectoryCrawled(crawlDirectory: CrawlDirectory) extends Event {
    override def eventType: EventType = DirectoryCrawl
  }
}