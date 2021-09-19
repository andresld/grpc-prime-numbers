package com.github.aldtid.grpc.prime.numbers.proxy.logging

import com.github.aldtid.grpc.prime.numbers.logging.model.Tag
import com.github.aldtid.grpc.prime.numbers.logging.implicits.model._


object tags {

  val launcherTag: Tag = "LAUNCHER".asTag
  val routerTag: Tag = "ROUTER".asTag
  val primesTag: Tag = "PRIMES".asTag

}
