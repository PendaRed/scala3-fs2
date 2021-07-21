package com.jgibbons.fs2.d

import cats.effect.IO
import fs2._
import cats.effect.unsafe.implicits.global

@main def EgPull =
  def callARestEndPoint() = IO{s"""{"time":${System.currentTimeMillis()}}"""}
  def transformData(jsonPayload:String) = jsonPayload.hashCode
  case class SomeData(i:Int)
  def moreRestDataFromId(id:Int) = Stream.eval(IO{SomeData(id*100+(Math.random()*100).toInt)}).repeatN(5)

  val aPureStream: Stream[Pure, Int] = Stream(1,2,3)
  val anEffectfulStream: Stream[IO, Int] = aPureStream.covary[IO]

  val anotherEffectfulStream: Stream[IO, String] = Stream.eval(callARestEndPoint())

  // Why effectful stream, because maybe you get 70 million data items, and you
  // are just transforming them before sending into a kafka producer
  val transformedEffectfulStream: Stream[IO, Int] = anotherEffectfulStream.map(transformData)

  // Lets say the int is an id for retrieving another stream of things
  // note we have a stream being returned, so the flatMap merges them
  val newStream: Stream[IO, SomeData] = transformedEffectfulStream.flatMap(id=> moreRestDataFromId(id))

  newStream.compile.drain.unsafeRunSync()