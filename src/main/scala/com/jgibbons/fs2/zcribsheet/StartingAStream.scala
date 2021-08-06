package com.jgibbons.fs2.zcribsheet

import cats.effect.{IO, SyncIO}

@main def StartingAStream =
  def showName(s:String) = println(
    s"""
       |$s
       |${"="*s.length}""".stripMargin)

  def charGen() = scala.util.Random.nextPrintableChar()
  def syncStreamGen() =
    val str = fs2.Stream.eval( SyncIO{charGen()} )
      .repeatN(3)
      .evalTap(c=> SyncIO{println(s"${Thread.currentThread().getName()} Sync $c")} )
    str.compile.drain

  def usingUnsafeRunSync() =
    showName("usingUnsafeRunSync")
    // Output shows it all runs on main thread
    //main Sync 7
    //main Sync X
    //main Sync |
    syncStreamGen().unsafeRunSync

  def asyncStreamGen() =
    val str = fs2.Stream.eval( IO{charGen()} )
      .repeatN(5)
      .parEvalMap(3)(c=> IO{println(s"${Thread.currentThread().getName()} Async $c")} )
    str.compile.drain

  // is the stream contains async things then it will block
  def usingUnsafeRunSyncOnIO() =
    import cats.effect.unsafe.implicits.global
    showName("usingUnsafeRunSyncOnIO on an IO with parallel")
    // Output shows runs on a compute thread
    //io-compute-2 Async L
    //io-compute-0 Async t
    //io-compute-3 Async ,
    //io-compute-2 Async w
    //io-compute-1 Async j
    asyncStreamGen().unsafeRunSync

  def infiniteStreamGen() =
    import scala.concurrent.duration.*
    val str = fs2.Stream.eval( IO{charGen()} )
      .metered(500.millis)
      .repeat
      .evalTap(c=> IO{println(s"${Thread.currentThread().getName()} $c")} )
    str.compile.drain

  // Useful to run a test and then cancel - eg cancel a Kafka consumer
  def usingAFibre() =
    showName("usingAFibre")
    // Output
    //io-compute-2 }
    //io-compute-2 M
    //io-compute-0 &
    //io-compute-0 j
    import cats.effect.unsafe.implicits.global
    val str = infiniteStreamGen()
    val fibre = str.start.unsafeRunSync
    Thread.sleep(4000)
    fibre.cancel

  usingUnsafeRunSync()
  usingUnsafeRunSyncOnIO()
  usingAFibre()
