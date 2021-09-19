package com.github.aldtid.grpc.prime.numbers.generator.logging

import com.github.aldtid.grpc.prime.numbers.logging.{BaseProgramLog, Loggable}
import com.github.aldtid.grpc.prime.numbers.protobuf.primes.PrimesRequest

import io.grpc.Metadata


trait ProgramLog[L] extends BaseProgramLog[L] {

  // Supported gRPC types
  implicit val primesRequestLoggable: Loggable[PrimesRequest, L]
  implicit val metadataLoggable: Loggable[Metadata, L]

}
