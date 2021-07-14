package com.jgibbons.fs2.c

import cats.effect.IO

import java.util.concurrent.atomic.AtomicLong

case class Message(offset: Long, body: String)

case class DummyKafka(initialOffset:Long) {
  val offset = AtomicLong(initialOffset)

  def setOffset(o:Long) = offset.set(o)

  def messageStore() =
    IO {
      val count = offset.get()+1
      offset.set(count)
      println(s"${Thread.currentThread().getName()} Message offset $count")
      Message(count, "I am a message")
    }
}
