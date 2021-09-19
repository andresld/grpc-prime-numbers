package com.github.aldtid.grpc.prime.numbers.generator.logging

import com.github.aldtid.grpc.prime.numbers.logging.implicits.model._
import com.github.aldtid.grpc.prime.numbers.logging.model.Message


object messages {

  // ---- LAUNCHER -----
  val loadingConfiguration: Message = "loading configuration".asMessage
  val configurationLoaded: Message = "configuration loaded".asMessage
  val configurationErrors: Message = "configuration had errors".asMessage
  val serverThreadPool: Message = "creating gRPC server thread pool".asMessage
  val startingServer: Message = "starting the server".asMessage

}
