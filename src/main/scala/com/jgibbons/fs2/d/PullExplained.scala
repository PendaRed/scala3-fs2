package com.jgibbons.fs2.d

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import fs2._

import java.util.concurrent.atomic.AtomicInteger

/**
 * July 2021, FS2 pull vital APIs explained by example.
 *
 * Pull is a monad in its last parameter, so you can call map on its 3rd type.
 * BUT, to get back to a stream you use Pull.output as that creates a Pull which emits and
 * then you can convert it back to a stream using .stream.  ie only Pulls which emit values can
 * be converted back to streams.
 */
@main def PullExplained =
  // dupCount represents a database or some other global resource
  val dupCount = AtomicInteger(0)

  def eg1StatefulStream(): Pipe[IO, Option[Long], Option[Long]] = {
    // The vital >> operator is vital for 'recursion' as it takes a function argument and defers it
    // which means fs2 can schedule it to run, so it not actually recursive on the stack
    def filterAndInsert(s: fs2.Stream[IO, Option[Long]], lastVal: Option[Long]): Pull[IO, Option[Long], Unit] = {
      s.pull // A pull exposes lots of great helpers which can work with chunk arrays
        .uncons1 // for instance, I only want to work with one item from the stream at a time
        .flatMap {
          case v@Some(Some(l), st) if l == 1L =>
            // Convert 1 to 101 then >> which takes a function param
            Pull.output1(Some(101L)) >> filterAndInsert(st, Some(l))
          case v@Some(Some(l), st) if l == 2L =>
            // Pull.pure(None) means remove the thing from the stream, so strip out 2 from the stream
            Pull.pure(None) >> filterAndInsert(st, Some(l))
          case v@Some(l, st) if l == lastVal =>
            // Do BAD external state stuff!  like databases and so on
            if (dupCount.incrementAndGet() % 5 == 0) // every 5th dupe output -1,-2,-3 into the stream
            // Insert 3 items for the 5th duplicate
              Pull.output(Chunk(Some(-1L), Some(-2L), Some(-3L))) >> filterAndInsert(st, l)
            else
            // Strip out any duplicates from the stream
              Pull.pure(None) >> filterAndInsert(st, l)
          case Some(l, st) =>
            // Keep the element unchanged.
            Pull.output1(l) >> filterAndInsert(st, l)
          case None =>
            // The stream is done, so am I
            Pull.done
        }
    }

    // Call the helper, which returns a Pull which has emitted values, so can call .stream on it.
    // Note that I am returning a function here, and this function is they Pipe
    in => filterAndInsert(in, None).stream
  }

  Stream(1, 2, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4)
    .map { i => Some(i.toLong) } // Convert them to Option[Long]
    .covary[IO] // change from a pure stream to an effectful stream
    .through(eg1StatefulStream()) // If was a pure stream could use throughPure, this needs a pipe, in=>out
    .map(r =>
      println(s"$r")
        r
    )
    .compile
    .drain
    .unsafeRunSync

/* output is
Some(101)    // ie replaced 1
Some(3)      // dropped 2 and then output 3
Some(-1)     // removed the dups until the 5th one when we insert new elements
Some(-2)
Some(-3)
Some(4)      // just output 4
Some(-1)     // again a 5th dup
Some(-2)
Some(-3)
 */

