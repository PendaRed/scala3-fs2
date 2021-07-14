package com.jgibbons.fs2.b

import cats.effect.unsafe.implicits.global
import cats.effect.{IO, IOApp}
import fs2.concurrent.SignallingRef
import fs2.{Pure, Stream}

import scala.concurrent.duration._

@main def StartStopAndRestart =
  var reset = false
  var count = 0
  case class Message(offset: Int, body: String)

  def messageStore() =
    IO {
      if (reset) {
        reset = false
        count=0
      }
      count += 1
      println(s"${Thread.currentThread().getName()} Message offset $count")
      Message(count, "I am a message")
    }

  val restartable =
    SignallingRef[IO, Boolean](false).flatMap { signal =>
      val s1 = Stream.repeatEval(messageStore()).metered(1.second).interruptWhen(signal)
      val s2 = Stream.sleep[IO](5.seconds).flatMap(f => {
        println(s"${Thread.currentThread().getName()} Interrupting!")
        reset=true
        Stream.eval(signal.set(true))
      })
      s1.concurrently(s2).compile.drain
    }

  Stream.eval(restartable).repeat.compile.drain.unsafeRunSync()
