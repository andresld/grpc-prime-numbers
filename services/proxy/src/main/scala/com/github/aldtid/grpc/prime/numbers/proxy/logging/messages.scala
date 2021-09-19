package com.github.aldtid.grpc.prime.numbers.proxy.logging

import com.github.aldtid.grpc.prime.numbers.logging.implicits.model._
import com.github.aldtid.grpc.prime.numbers.logging.model.Message


object messages {

  // ----- LAUNCHER -----
  val loadingConfiguration: Message = "loading configuration".asMessage
  val configurationLoaded: Message = "configuration loaded".asMessage
  val configurationErrors: Message = "configuration had errors".asMessage
  val creatingPrimesClient: Message = "creating primes gRPC client".asMessage
  val clientThreadPool: Message = "creating gRPC client thread pool".asMessage
  val serverThreadPool: Message = "creating HTTP server thread pool".asMessage
  val startingServer: Message = "starting the server".asMessage

  // ----- GENERAL ROUTES -----
  val incomingRequest: Message = "incoming request".asMessage
  val outgoingResponse: Message = "outgoing response".asMessage

  // ----- PRIMES -----
  val primesRequest: Message = "requesting for primes".asMessage
  val primesResponse: Message = "received primes response".asMessage

}
