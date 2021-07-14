package com.jgibbons.fs2.a

import cats.effect.{IO, IOApp}
import fs2.{Pure, Stream}

import scala.concurrent.duration.FiniteDuration

object ZipEg extends IOApp.Simple :
  def makeStream(): Stream[IO, (FiniteDuration, Unit)] =
    import scala.concurrent.duration._
    def put(s:String) = IO{println(s)}
    def printRange = Stream.range(1, 10).evalMap(s=>put(""+s))
    def seconds = Stream.awakeEvery[IO](1.second)
    seconds.zip(printRange)

  override def run =
    val k = makeStream().compile.drain
    k