package com.jgibbons.fs2.zcribsheet

import cats.effect.{IO, SyncIO}

import scala.util.Random
import scala.concurrent.duration.*
import cats.effect.IO, cats.effect.unsafe.implicits.global

/**
 * This shows never ending streams running on compute threads, the output is
 * io-compute-1 A: May
 * io-compute-2 D: Apr
 * io-compute-0 C: Jun
 * io-compute-3 B: Nov
 * io-compute-0 D: Apr
 * io-compute-3 B: Nov
 * io-compute-1 A: May
 * io-compute-2 C: Jun
 * io-compute-1 A: May
 * ...
 */
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

  val ownerStr1 = fs2.Stream.eval({  // Doing fs2.Stream.eval means we need the compile.drain below
    val strA = monStream("A:")
    val strB = monStream("B:")

    strA.concurrently(strB).compile.drain // the eval expects an IO, which is provided by the compile.drain
  })
  val ownerStr2 = {
    val strC = monStream("C:")
    val strD = monStream("D:")

    strC.concurrently(strD)  // no compile.drain needed, as no stream.eval
  }

  val s = ownerStr1.concurrently(ownerStr2)
  s.compile.drain.unsafeRunSync


