package com.jgibbons.fs2.zcribsheet

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import fs2.Stream
import fs2.concurrent.SignallingRef

import scala.concurrent.duration.*

/**
 * An example of running part of the stream in parallel with 3 in parallel say.
 * The out put is:
io-compute-3 processing 10 values
io-compute-1 processing 10 values
io-compute-2 processing 10 values
io-compute-3 done 10 values
io-compute-1 done 10 values
io-compute-2 done 10 values
io-compute-1 processing 10 values
io-compute-0 processing 10 values
io-compute-3 processing 10 values
io-compute-1 done 10 values
io-compute-1 processing 10 values
io-compute-0 done 10 values
io-compute-3 done 10 values
io-compute-2 processing 10 values
io-compute-3 processing 10 values
io-compute-1 done 10 values
io-compute-1 processing 10 values
io-compute-3 done 10 values
io-compute-2 done 10 values
io-compute-3 processing 10 values
io-compute-0 processing 10 values
io-compute-1 done 10 values
io-compute-2 processing 10 values
io-compute-3 done 10 values
io-compute-0 done 10 values
io-compute-2 done 10 values
 */
@main def StartAnAsyncProcessor() = {
  def stopAfter[A](f: FiniteDuration): fs2.Stream[IO, A] => fs2.Stream[IO, A] =
    in => {
      def close(s: SignallingRef[IO, Boolean]): fs2.Stream[IO, Unit] =
        fs2.Stream.sleep[IO](f) ++ Stream.eval(s.set(true))

      fs2.Stream.eval(SignallingRef[IO, Boolean](false)).flatMap { signal =>
        in.interruptWhen(signal).concurrently(close(signal))
      }
    }

  def genRnd() = IO {
    Math.random()
  }

  def doProcess(arr: Array[Double]): IO[Array[Int]] = IO {
    println(s"${Thread.currentThread().getName} processing ${arr.size} values")
    Thread.sleep(3000)
    val ret = arr.map(d => (d * 100).toInt)
    println(s"${Thread.currentThread().getName} done ${arr.size} values")
    ret
  }

  val infStream = fs2.Stream.eval(genRnd()).repeat

  infStream.chunkN(10).parEvalMap(3)(chks => {
    val data = chks.toArray
    doProcess(data)
  })
    .through(stopAfter(10.seconds))
    .compile.drain.unsafeRunSync

  Thread.sleep(10000)
}
