package com.jgibbons.fs2.zcribsheet

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import fs2.Stream
import fs2.concurrent.SignallingRef

import scala.concurrent.duration.*

/**
 * Takes the first emitted value
 * 1 2 1 2 2 1 1 2 2 1 1 2 2 1 .....
 */
@main def MergeFs2Streams =
  val s1 = fs2.Stream.eval(IO{1}).metered(100.millis).repeatN(20)
  val s2 = fs2.Stream.eval(IO{2}).metered(100.millis).repeatN(20)

  val s3 = s1.merge(s2)
  val s4 = s3.map(x=>{println(x); x})
  val s5 = s4.compile.drain.unsafeRunSync
