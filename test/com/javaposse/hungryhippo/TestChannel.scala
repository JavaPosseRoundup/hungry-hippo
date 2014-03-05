package com.javaposse.hungryhippo

import play.api.libs.iteratee.{Input, Concurrent}
import scala.collection.mutable

class TestChannel[E] extends Concurrent.Channel[E] {
  private var ended = false
  private var chunks = new mutable.MutableList[Input[E]]()

  override def end() {
    ended = true
  }

  override def end(e: Throwable): Unit = end()

  override def push(chunk: Input[E]) {
    chunks += chunk
  }

  def last = chunks.last

  def isEnded = ended
}
