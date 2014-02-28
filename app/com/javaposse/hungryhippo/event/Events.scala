package com.javaposse.hungryhippo.event

import com.javaposse.hungryhippo.actors.CrawlState

object Events {
  val AllEventTypes = Set(CrawlStateChange)

  trait Event {
    def eventType: EventType
  }

  sealed abstract class EventType
  
  case object CrawlStateChange extends EventType
  
  case class CrawlStateChanged(crawlState: CrawlState) extends Event {
    def eventType = CrawlStateChange
  }
}