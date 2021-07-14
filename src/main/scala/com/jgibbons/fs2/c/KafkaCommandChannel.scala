package com.jgibbons.fs2.c


import cats.effect.{IO, IOApp}
import fs2.concurrent.Topic
import fs2.{Pure, Stream}

import scala.concurrent.duration._

/*
* One kafka topic receives commands which can tell the other kafka consumer to
* disconnect and reset the read offset on reconnect
 */
object KafkaCommandChannel :
  def createConsumerStreams(): IO[Unit] =
    val statefulCreateMessageConsumer = KafkaConsumerResetable()

    // command channel randomly generates a new offset every 5 seconds, like it has received a reset command
    val kafkaConsumers = Topic[IO, Long].flatMap{ restartTopic =>
      val commandChannel = Stream.repeatEval(IO{(Math.random()*400).toLong})
        .metered(5.second)
        .map(offset => {
          println(s"${Thread.currentThread().getName()} Interrupting to offset $offset!")
          offset
        })
        .through(restartTopic.publish) // This will cause the other kafka to stop and then it will get restarted.

      // Note the .repeat, means when it terminates it will restart
      val s2 = statefulCreateMessageConsumer.restartable(restartTopic)
      val restartable = Stream.eval(s2).repeat
      commandChannel.concurrently(restartable).compile.drain
    }
    kafkaConsumers


