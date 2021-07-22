package com.jgibbons.fs2.a

import fs2._
import scala.concurrent.duration._
import cats.effect.IO
import cats.implicits._
import cats.effect.unsafe.implicits.global

@main def NestedStreams =
  val str1 = Stream(1,2,3,4).covary[IO].metered(1.second).evalTap(l=> IO{
    println("STR  "+l)
  })

  Stream("A", "B", "C")
    .covary[IO]
    .evalTap(l=> IO{
      println("1 "+l)
    })
    .evalTap{l=> str1.compile.drain }
    .evalTap(l=> IO{
      println("2 "+l)
    })

    .compile.drain.unsafeRunSync()
