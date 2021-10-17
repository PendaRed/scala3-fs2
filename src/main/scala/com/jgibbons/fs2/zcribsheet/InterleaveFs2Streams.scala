package com.jgibbons.fs2.zcribsheet

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import scala.concurrent.duration.*

@main def InterleaveFs2Streams = {
  val s1 = fs2.Stream.eval(IO{1}).metered(100.millis).repeatN(20)
  val s2 = fs2.Stream.eval(IO{2}).metered(100.millis).repeatN(20)

  val s3 = s1.interleave(s2)
  val s4 = s3.map(x=>{println(x); x})
  val s5 = s4.compile.drain.unsafeRunSync
}
