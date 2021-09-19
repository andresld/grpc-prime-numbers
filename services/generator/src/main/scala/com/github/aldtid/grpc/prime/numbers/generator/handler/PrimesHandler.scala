package com.github.aldtid.grpc.prime.numbers.generator.handler

import com.github.aldtid.grpc.prime.numbers.logging.implicits.all._
import com.github.aldtid.grpc.prime.numbers.generator.logging.ProgramLog
import com.github.aldtid.grpc.prime.numbers.generator.logging.messages._
import com.github.aldtid.grpc.prime.numbers.generator.logging.tags.primesTag
import com.github.aldtid.grpc.prime.numbers.protobuf.primes.{PrimesFs2Grpc, PrimesRequest, PrimesResponse}

import fs2.Stream
import io.grpc.Metadata
import org.typelevel.log4cats.Logger

import scala.annotation.tailrec


trait PrimesHandler[F[_]] extends PrimesFs2Grpc[F, Metadata]

object PrimesHandler {

  def isPrime(number: Long): Boolean = {

    @tailrec
    def run(candidates: Iterator[Long]): Boolean =
      if (candidates.isEmpty) true
      else if (number % candidates.next() != 0) run(candidates)
      else false

    number == 2 || (number % 2 != 0 && run((3L to (number / 3, 2)).iterator))

  }

  def calculatePrimes[F[_] : Logger, L](request: PrimesRequest, metadata: Metadata)
                                       (implicit pl: ProgramLog[L]): Stream[F, PrimesResponse] = {

    import pl._

    Stream.eval(Logger[F].info(primeGeneration |+| request |+| metadata |+| primesTag)) >>
      Stream.iterable[F, Long](2L to request.number).filter(isPrime).map(PrimesResponse(_))

  }

  def default[F[_] : Logger, L : ProgramLog]: PrimesHandler[F] =
    (request: PrimesRequest, metadata: Metadata) => PrimesHandler.calculatePrimes(request, metadata)

}
