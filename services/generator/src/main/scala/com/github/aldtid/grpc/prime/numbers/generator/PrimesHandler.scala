package com.github.aldtid.grpc.prime.numbers.generator

import com.github.aldtid.grpc.prime.numbers.generator.logging.ProgramLog
import com.github.aldtid.grpc.prime.numbers.protobuf.primes.{PrimesFs2Grpc, PrimesRequest, PrimesResponse}

import fs2.Stream
import io.grpc.Metadata


trait PrimesHandler[F[_]] extends PrimesFs2Grpc[F, Metadata]

object PrimesHandler {

  def calculatePrimes[F[_], L : ProgramLog](request: PrimesRequest, metadata: Metadata): Stream[F, PrimesResponse] =
    Stream(PrimesResponse(request.number))

  def default[F[_], L : ProgramLog]: PrimesHandler[F] =
    (request: PrimesRequest, metadata: Metadata) => PrimesHandler.calculatePrimes(request, metadata)

}
