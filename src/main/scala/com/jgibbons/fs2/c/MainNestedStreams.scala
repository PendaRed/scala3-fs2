package com.jgibbons.fs2.c

import cats.effect.IO
import cats.effect.unsafe.implicits.global

@main def MainNestedStreams =
  KafkaCommandChannel.createConsumerStreams().unsafeRunSync
