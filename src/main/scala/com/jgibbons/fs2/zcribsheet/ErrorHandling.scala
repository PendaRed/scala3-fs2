package com.jgibbons.fs2.zcribsheet

import cats.effect.IO
import cats.effect.unsafe.implicits.global

/**
 * Inside IO's do not throw execeptions, instead use IO.raiseError()
 * output:
 * Thread:[main] failingOutsideIO Earlier Traditional Try Catch Block
 * Thread:[io-compute-1] failingOutsideIO Traditional Try Catch Block
 * Async failingOutsideIO workeed
 * Thread:[io-compute-1] failingInsideIO Earlier The correct way!
 * Thread:[io-compute-1] failingInsideIO The correct way!
 * Async failingInsideIO workeed
 * Thread:[io-compute-1] failingProperly Earlier The correct way!
 * Thread:[io-compute-1] failingProperly The correct way!
 * Async failingProperly workeed
 */
@main def ErrorHandling() = {
  def failingOutsideIO(b: Boolean)() = if (b) IO {
    1
  } else throw new Exception("Not thrown inside the IO")

  def failingInsideIO(b: Boolean)() = IO {
    if (b) 10 else throw new Exception("Not thrown inside the IO")
  }

  def failingProperly(b: Boolean)(): IO[Int] = {
    val ret: IO[Int] =
      if (b) IO {
        100
      } else {
        val e = IO.raiseError[Int](new Exception("raiseError"))
        e
      }
    ret
  }

  def callMyFunction(debugStr: String, fn: () => IO[Int]) =
    try {
      val myIO = for {
        r <- fn()
      } yield r
      myIO.handleErrorWith {
        case ex: Exception =>
          println(s"Thread:[${Thread.currentThread().getName}] $debugStr Earlier The correct way!")
          IO {
            println(s"Thread:[${Thread.currentThread().getName}] $debugStr The correct way!")
            2
          }
      }
    } catch {
      case ex: Exception =>
        println(s"Thread:[${Thread.currentThread().getName}] $debugStr Earlier Traditional Try Catch Block")
        IO {
          println(s"Thread:[${Thread.currentThread().getName}] $debugStr Traditional Try Catch Block")
          3
        }
    }
  end callMyFunction

  callMyFunction("failingOutsideIO", failingOutsideIO(false)).unsafeRunAsync {
    case Left(ex) => println(s"Async failed with ${ex.getClass.getName}")
    case Right(i) => println(s"Async failingOutsideIO workeed")
  }
  Thread.sleep(1000)

  callMyFunction("failingInsideIO", failingInsideIO(false)).unsafeRunAsync{
    case Left(ex) => println(s"Async failed with ${ex.getClass.getName}")
    case Right(i) => println(s"Async failingInsideIO workeed")
  }
  Thread.sleep(1000)

  callMyFunction("failingProperly", failingProperly(false)).unsafeRunAsync {
    case Left(ex) => println(s"Async failed with ${ex.getClass.getName}")
    case Right(i) => println(s"Async failingProperly workeed")
  }
  Thread.sleep(1000)
}