package com.github.aldtid.grpc.prime.numbers.proxy.handler

import com.github.aldtid.grpc.prime.numbers.protobuf.primes.{PrimesFs2Grpc, PrimesRequest}
import com.github.aldtid.grpc.prime.numbers.proxy.logging.ProgramLog

import cats.Applicative
import cats.data.{EitherT, NonEmptyList}
import cats.effect.Clock
import fs2.Compiler
import io.grpc.Metadata
import org.typelevel.log4cats.Logger


trait PrimesHandler[F[_]] {

  def handlePrimes(number: Long): EitherT[F, String, NonEmptyList[Long]]

}

object PrimesHandler {

  def handlePrimes[F[_] : Applicative : Clock : Logger, L : ProgramLog](number: Long,
                                                                        primes: PrimesFs2Grpc[F, Metadata])
                                                                       (implicit C: Compiler[F, F]): EitherT[F, String, NonEmptyList[Long]] =
    EitherT.right(primes.calculatePrimes(PrimesRequest(number), new Metadata()).map(_.prime).compile.toList)
      .map(NonEmptyList.fromListUnsafe)

  def default[F[_] : Applicative : Clock : Logger, L : ProgramLog](primes: PrimesFs2Grpc[F, Metadata])
                                                                  (implicit C: Compiler[F, F]): PrimesHandler[F] =
    (number: Long) => handlePrimes(number, primes)

}
