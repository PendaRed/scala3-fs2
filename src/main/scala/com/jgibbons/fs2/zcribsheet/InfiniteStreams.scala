package com.jgibbons.fs2.zcribsheet

import cats.effect.{IO, SyncIO}

import scala.util.Random
import scala.concurrent.duration.*
import cats.effect.IO, cats.effect.unsafe.implicits.global

@main def InfiniteStreams =
  def monStream(nm:String) =
    val months = List("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    val infStream = fs2.Stream
      .emit( months(Random.nextInt(months.length)))
      .covary[IO]
      .metered(500.millis)
      .evalTap(m=>IO{println(s"${Thread.currentThread.getName()} $nm $m")})
      .repeat
    infStream
  end monStream

  val ownerStr1 = fs2.Stream.eval({
    val strA = monStream("A:")
    val strB = monStream("B:")

    strA.concurrently(strB).compile.drain
  })
  val ownerStr2 = fs2.Stream.eval({
    val strC = monStream("C:")
    val strD = monStream("D:")

    strC.concurrently(strD).compile.drain
  })
  ownerStr1.compile.drain.unsafeRunSync

//  val s = ownerStr1.concurrently(ownerStr2)
//  s.compile.drain.unsafeRunAsync

  Thread.sleep(60000)

