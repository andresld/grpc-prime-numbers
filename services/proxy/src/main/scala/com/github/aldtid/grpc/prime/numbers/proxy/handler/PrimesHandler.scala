package com.github.aldtid.grpc.prime.numbers.proxy.handler

import com.github.aldtid.grpc.prime.numbers.logging.implicits.all._
import com.github.aldtid.grpc.prime.numbers.protobuf.primes.{PrimesFs2Grpc, PrimesRequest, PrimesResponse}
import com.github.aldtid.grpc.prime.numbers.proxy.logging.ProgramLog
import com.github.aldtid.grpc.prime.numbers.proxy.logging.messages._
import com.github.aldtid.grpc.prime.numbers.proxy.logging.tags.primesTag

import cats.Monad
import cats.data.{EitherT, NonEmptyList}
import cats.effect.Clock
import fs2.Compiler
import io.grpc.Metadata
import org.typelevel.log4cats.Logger


/**
 * Defines the behaviour for 'prime' routes.
 *
 * Each function represents a handling function for one endpoint. Functions do not expose extra parameters (connections
 * to external services, logging instances...) or types (logging) as later router functions may get overloaded with
 * multiple parameters, making them harder to test and to deal with. Because of that, those instances (if required) must
 * be offered at handler creation time.
 *
 * @tparam F context type
 */
trait PrimesHandler[F[_]] {

  /**
   * Calculates all the prime numbers up to passed number.
   *
   * @param number target number to calculate prime numbers
   * @return the list of prime numbers or an error
   */
  def handlePrimes(number: Long): EitherT[F, String, NonEmptyList[Long]]

}

object PrimesHandler {

  /**
   * Uses a gRPC primes service to perform a call to extract the prime numbers for a target number.
   *
   * In case the returned list of primes is empty, an error is returned.
   *
   * @param number target number to generate primes
   * @param primes primes gRPC service to request for primes
   * @param C compiler instance
   * @param pl logging instances
   * @tparam F context type
   * @tparam L logging type to format
   * @return the primes list or an error
   */
  def handlePrimes[F[_] : Monad : Clock : Logger, L](number: Long,
                                                     primes: PrimesFs2Grpc[F, Metadata])
                                                    (implicit C: Compiler[F, F],
                                                     pl: ProgramLog[L]): EitherT[F, String, NonEmptyList[Long]] = {

    import pl._

    val request: PrimesRequest = PrimesRequest(number)

    for {

      _       <- EitherT.right(Logger[F].info(primesRequest |+| request |+| primesTag))
      start   <- EitherT.right(Clock[F].realTime)

      list    <- EitherT.right(primes.calculatePrimes(request, new Metadata()).compile.toList)

      end     <- EitherT.right(Clock[F].realTime)
      latency  = (end - start).toMillis.asLatency
      _       <- EitherT.right(Logger[F].info(primesResponse |+| request |+| latency |+| primesTag))

      pr      <- NonEmptyList.fromList(list).fold(EitherT.leftT[F, NonEmptyList[PrimesResponse]]("no primes"))(EitherT.rightT(_))

    } yield pr.map(_.prime)

  }

  /**
   * Default implementation for 'primes' endpoint handler.
   *
   * @param primes primes gRPC service to request for primes
   * @param C compiler instance
   * @tparam F context type
   * @tparam L logging type to format
   * @return the default handler implementation
   */
  def default[F[_] : Monad : Clock : Logger, L : ProgramLog](primes: PrimesFs2Grpc[F, Metadata])
                                                            (implicit C: Compiler[F, F]): PrimesHandler[F] =
    (number: Long) => handlePrimes(number, primes)

}
