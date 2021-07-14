package com.jgibbons.fs2.c

import cats.effect.unsafe.implicits.global
import cats.effect.{IO, IOApp}
import fs2.concurrent.SignallingRef
import fs2.{Pure, Stream}
import fs2.concurrent.Topic

import scala.concurrent.duration._
import java.util.concurrent.atomic.AtomicLong

case class KafkaConsumerResetable() :
  def restartable(topic: Topic[IO, Long]): IO[Unit] =
      val msgs = new DummyKafka(0)
      SignallingRef[IO, Boolean](false).flatMap { signal =>
      val s1 = Stream.repeatEval(msgs.messageStore()).metered(1.second).interruptWhen(signal)
      val s2 = topic.subscribe(2).flatMap(newOffset => {
        println(s"${Thread.currentThread().getName()} Interrupting!, new offset is $newOffset")
        msgs.setOffset(newOffset)
        Stream.eval(signal.set(true))
      })
      s1.concurrently(s2).compile.drain
    }
