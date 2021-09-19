package com.github.aldtid.grpc.prime.numbers.proxy.logging

import com.github.aldtid.grpc.prime.numbers.logging.{BaseProgramLog, Loggable}
import com.github.aldtid.grpc.prime.numbers.protobuf.primes.PrimesRequest

import org.http4s.{Request, Response}


trait ProgramLog[L] extends BaseProgramLog[L] {

  // Supported http4s types
  implicit def requestLoggable[F[_]]: Loggable[Request[F], L]
  implicit def responseLoggable[F[_]]: Loggable[Response[F], L]

  // Supported gRPC types
  implicit val primesRequestLoggable: Loggable[PrimesRequest, L]

}
