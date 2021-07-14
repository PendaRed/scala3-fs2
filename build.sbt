import sbt.Keys.libraryDependencies

val scala3Version = "3.0.0"

lazy val root = project
  .in(file("."))
  .settings(
    name := "scala3-fs2",
    version := "0.1.0",

    scalaVersion := scala3Version,

    // https://mvnrepository.com/artifact/co.fs2/fs2-core
	libraryDependencies += "co.fs2" % "fs2-core_2.13" % "3.0.6",
	// optional I/O library
	libraryDependencies += "co.fs2" % "fs2-io_2.13" % "3.0.6",

	// optional reactive streams interop
	libraryDependencies += "co.fs2" % "fs2-reactive-streams_2.13" % "3.0.6",

  // https://mvnrepository.com/artifact/org.typelevel/cats-effect
  libraryDependencies += "org.typelevel" % "cats-effect_3" % "3.1.1",

  )
