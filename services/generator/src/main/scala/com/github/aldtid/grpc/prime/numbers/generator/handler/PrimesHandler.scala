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


/**
 * Implements the primes service defined as a protobuf contract.
 *
 * @tparam F context type
 */
trait PrimesHandler[F[_]] extends PrimesFs2Grpc[F, Metadata]

object PrimesHandler {

  /**
   * Checks if passed number is a prime or not.
   *
   * Some optimizations have been made to save check operations:
   *   - an early check for passed number being 2 that enables to filter all the even numbers
   *   - a check from every odd number from 3 to number/3, because there cannot be other divisors after that point
   *
   * @param number number to check
   * @return true if the number is prime or false otherwise
   */
  def isPrime(number: Long): Boolean = {

    @tailrec
    def run(candidates: Iterator[Long]): Boolean =
      if (candidates.isEmpty) true
      else if (number % candidates.next() != 0) run(candidates)
      else false

    number == 2 || (number % 2 != 0 && run((3L to (number / 3, 2)).iterator))

  }

  /**
   * Calculates all the prime numbers up to passed request number.
   *
   * @param request primes request
   * @param metadata request metadata
   * @param pl logging instances
   * @tparam F context type
   * @tparam L logging type to format
   * @return a stream of all prime numbers up to passed number
   */
  def calculatePrimes[F[_] : Logger, L](request: PrimesRequest, metadata: Metadata)
                                       (implicit pl: ProgramLog[L]): Stream[F, PrimesResponse] = {

    import pl._

    Stream.eval(Logger[F].info(primeGeneration |+| request |+| metadata |+| primesTag)) >>
      Stream.iterable[F, Long](2L to request.number).filter(isPrime).map(PrimesResponse(_))

  }

  /**
   * Default implementation for the handler.
   *
   * @tparam F context type
   * @tparam L logging type to format
   * @return de default implementation of the handler
   */
  def default[F[_] : Logger, L : ProgramLog]: PrimesHandler[F] =
    (request: PrimesRequest, metadata: Metadata) => PrimesHandler.calculatePrimes(request, metadata)

}
