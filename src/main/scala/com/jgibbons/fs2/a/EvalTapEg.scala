package com.jgibbons.fs2.a

import cats.effect.*
import fs2.*
import cats.effect.unsafe.implicits.global

@main def EvalTapEg =
  case class MyData(id:Int, fname:String, sname:String)
  // This is a vital part of fs2, as it will schedule any types of IO you want without changing
  // the stream itself
  def anEffectfulFunction(i:Int) : IO[MyData] = {
    for {
      pers <- IO{MyData(i, "Caladan", "Brood")}
      _ = println(pers)
    } yield pers
  }

  val str = fs2.Stream(1,2,3)
    .covary[IO]
    .evalTap(n=> anEffectfulFunction(n))
    .map(n=>{
      println(s"The stream is still ints - $n")
      n+100
    })
    .evalTap(n=> IO{println(s"The stream is still ints - $n")})
    .compile
    .drain

  str.unsafeRunSync

//MyData(1,Caladan,Brood)
//The stream is still ints - 1
//The stream is still ints - 101
//MyData(2,Caladan,Brood)
//The stream is still ints - 2
//The stream is still ints - 102
//MyData(3,Caladan,Brood)
//The stream is still ints - 3
//The stream is still ints - 103
