package com.jgibbons.fs2.a

import cats.effect.{IO}
import fs2.{Pure, Stream}

@main def EmitEg =
  def emitEg() =
    import cats.effect.SyncIO
    val pureStream = Stream(1,2,3)  // Stream[Pure, Int]
    pureStream.foreach(i=> SyncIO(println(s"I can count to $i"))).compile.drain

  emitEg().unsafeRunSync()
