package com.jgibbons.fs2.zcribsheet

import cats.effect.IO
import fs2.Pure

@main def StreamingValuesBasedOnPrevious =
  import cats.effect.unsafe.implicits.global

  val seven: fs2.Stream[Pure, Int] = fs2.Stream.iterate(1)(_*2).take(7)
  val reversed: fs2.Stream[Pure, Int] = fs2.Stream.iterate(64)(_/2).take(7)

  (seven ++ reversed)
    .evalTap(v=>IO{println(v)})
    .compile.drain
    .unsafeRunSync