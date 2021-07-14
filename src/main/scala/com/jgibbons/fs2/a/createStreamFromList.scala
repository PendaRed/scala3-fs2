package com.jgibbons.fs2.a

import cats.effect.{IO, SyncIO}
import fs2.{Pure, Stream}

@main def CreateStreamFromList =
  val g: Stream[Pure, Int] = Stream.emits(List(1,2,3))
  val s: SyncIO[Unit] = g.foreach(i=> SyncIO(println(i)) ).compile.drain

  import cats.effect.unsafe.implicits.global
  s.unsafeRunSync
  println("hi")