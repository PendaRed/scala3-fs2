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

//1 A
//STR  1
//STR  2
//STR  3
//STR  4
//2 A
//1 B
//STR  1
//STR  2
//STR  3
//STR  4
//2 B
//1 C
//STR  1
//STR  2
//STR  3
//STR  4
//2 C