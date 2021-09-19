package com.github.aldtid.grpc.prime.numbers.proxy.logging

import com.github.aldtid.grpc.prime.numbers.logging.implicits.model._
import com.github.aldtid.grpc.prime.numbers.logging.model.Message


object messages {

  // ----- LAUNCHER -----
  val creatingPrimesClient: Message = "creating primes gRPC client".asMessage
  val startingServer: Message = "starting the server".asMessage

  // ----- GENERAL ROUTES -----
  val incomingRequest: Message = "incoming request".asMessage
  val outgoingResponse: Message = "outgoing response".asMessage

}
