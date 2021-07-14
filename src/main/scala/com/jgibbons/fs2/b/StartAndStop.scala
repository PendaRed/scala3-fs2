package com.jgibbons.fs2.b

import cats.effect.{IO, IOApp}
import fs2.{Pure, Stream}
import fs2.concurrent.SignallingRef
import scala.concurrent.duration._
import cats.effect.unsafe.implicits.global

@main def StartAndStop =
  var count=0
  case class Message(offset:Int, body:String)
  def messageStore() =
    IO {
      count += 1
      println(s"${Thread.currentThread().getName()} Message offset $count")
      Message(count, "I am a message")
    }

  SignallingRef[IO, Boolean](false).flatMap {signal =>
    val s1 = Stream.repeatEval(messageStore()).metered(1.second).interruptWhen(signal)
    val s2 = Stream.sleep[IO](5.seconds).flatMap(f=>{
      println(s"${Thread.currentThread().getName()} Interrupting!")
      Stream.eval(signal.set(true))
    })
    s1.concurrently(s2).compile.toVector
  }.unsafeRunSync()
