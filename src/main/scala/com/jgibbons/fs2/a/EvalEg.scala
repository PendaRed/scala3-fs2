package com.jgibbons.fs2.a

import fs2.Stream
import cats.effect.IO
import cats.effect.unsafe.implicits.global

@main def EvalEg =
  def Random() = IO{ Math.random()*10}

  val l = Stream.eval( Random()).repeatN(10)
    .map(r=>
      println(r)
      r
    )
    .compile.drain

  l.unsafeRunSync