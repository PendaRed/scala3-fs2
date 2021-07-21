package com.jgibbons.fs2.d

import cats.effect.{IO, SyncIO}
import fs2.{INothing, Pull, Pure, Stream}
import cats.effect.unsafe.implicits.global

@main def NowPulls =
  import fs2._

  def tk[F[_],O](n: Long): Pipe[F,O,O] = {
    def go(s: Stream[F,O], n: Long): Pull[F,O,Unit] = {
      s.pull.uncons.flatMap {
        case Some((hd,tl)) =>
          hd.size match {
            case m if m <= n => Pull.output(hd) >> go(tl, n - m)
            case _ => Pull.output(hd.take(n.toInt)) >> Pull.done
          }
        case None => Pull.done
      }
    }
    // ie we return a pipe, which is a type defined as Stream[F, I] => Stream[F, O]
    in => go(in,n).stream
  }

  val l = Stream(1,2,3,4).through(tk(2)).toList
  println(s"List = $l")
  // res34: List[Int] = List(1, 2)
  val s = Stream(1,2,3,4)
    .covary[IO]
    .through(tk(2))
    .map(r=> {
      println(s"I can count to $r")
      r
    }).compile.drain
  s.unsafeRunSync()

