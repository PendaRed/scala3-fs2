package com.jgibbons.fs2.zcribsheet

@main def simpleApis =
  def showName(s:String) = println(
    s"""
       |$s
       |${"="*s.length}""".stripMargin)

  def emitNumbers() =
    import cats.effect.SyncIO
    import fs2.{Pure, Stream}
    showName("emitNumbers")
    // Outputs
    //My number is 1
    //My number is 2
    //My number is 3
    val str1 = fs2.Stream(1,2,3)        // Stream[fs2.Pure, Int]
      .map(i=>s"My number is $i")       // Stream[fs2.Pure, String]
      .foreach(s=>SyncIO(println(s)))   // Stream[SyncIO, fs2.INothing]
      .compile                          // Stream.CompileOps[SyncIO, Nothing, fs2.INothing]
      .drain                            // value of the target effect, in this case Nothing
    str1.unsafeRunSync
  end emitNumbers

  def emitFromAList() =
    import cats.effect.SyncIO
    import fs2.{Pure, Stream}
    showName("\nemitFromAList")
    // outputs
    //1
    //2
    //3
    val str1 = Stream.emits(List(1,2,3))   // Stream[Pure, Int]
      .foreach(i=> SyncIO(println(i)) )    // Stream[SyncIO, fs2.INothing]
      .compile.drain

    str1.unsafeRunSync
  end emitFromAList

  def evalsAnEffectProducingValues() =
    import cats.effect.SyncIO
    import fs2.{Pure, Stream}
    showName("evalsAnEffectProducingValues")
    def Random() = SyncIO{ Math.random()*10}

    val l = Stream.eval( Random())  // Stream[SyncIO, Double]
      .repeatN(2)
      .map(r=>
        println(s"Map to do something, and then return the original is not correct [$r]")
        r
      )
      .evalTap(r=>
        SyncIO{println(s"instead use evalTap, which preserves the stream but runs the effect [$r]")}
      )                             // Still Stream[SyncIO, Double]
    l.compile.drain.unsafeRunSync
  end evalsAnEffectProducingValues

  def exampleOfAs() = {
    import cats.effect.SyncIO
    import fs2.Stream
    showName("exampleOfAs")
    // outputs
    //I am a side effect
    //Madness [l]
    val m = SyncIO{println("I am a side effect"); 1}
    val m1: SyncIO[String] = m.as("l")  // Look I can change the content and type of the SyncIO
    val str = Stream.eval(m1).evalTap(s=>SyncIO{println(s"Madness [${s}]")})
    str.compile.drain.unsafeRunSync
  }

  //    import cats.effect.unsafe.implicits.global

  // Now call them
  emitNumbers()
  emitFromAList()
  evalsAnEffectProducingValues()
  exampleOfAs()