package com.jgibbons.fs2.zcribsheet

import scala.concurrent.duration._
import cats.effect.IO

import cats.effect.unsafe.implicits.global
import fs2.concurrent.SignallingRef
import fs2.{Pure, Stream}

/**
 * From Fabio's talk in 2018, a great illustration
 */
@main def FabioComposableStreams() =

  def stopAfter[A](f: FiniteDuration): fs2.Stream[IO, A] => fs2.Stream[IO, A] =
    in => {
      def close(s: SignallingRef[IO, Boolean]): fs2.Stream[IO, Unit] =
        fs2.Stream.sleep[IO](f) ++ Stream.eval(s.set(true))

      fs2.Stream.eval(SignallingRef[IO, Boolean](false)).flatMap { signal =>
        in.interruptWhen(signal).concurrently(close(signal))
      }
      }

  fs2.Stream
    .repeatEval(IO(println("hello")))
    .through(stopAfter(2.seconds))
    .compile.drain
    .unsafeRunSync
