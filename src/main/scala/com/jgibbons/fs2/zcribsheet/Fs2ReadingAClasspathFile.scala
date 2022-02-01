package com.jgibbons.fs2.zcribsheet

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import fs2.text
import fs2.io.file.{Files, Path}

import java.nio.file.{FileSystems, Paths}

// Example of fs2 streaming a file from the classpath
@main def Fs2ReadingAClasspathFile = {
  def readFileFromClasspath(filePath:String): fs2.Stream[IO, String] = {
//    val path=Path(filePath) // use this if have full path
    val resourceUrl = getClass.getClassLoader.getResource("txt1.txt")
    val path = Paths.get(resourceUrl.toURI)
    val fs2Path=Path.fromNioPath(path)

    val linesNoComments = Files[IO].readAll(fs2Path)
      .through(text.utf8.decode)
      .through(text.lines)
      .map(_.trim)
      .filter(!_.startsWith("#"))
      .filter(!_.startsWith("//"))
      .covary[IO]

    linesNoComments
  }

  readFileFromClasspath("txt1.txt")
    .evalTap(l=>IO{println(l)})
    .compile.drain
    .unsafeRunSync

}
