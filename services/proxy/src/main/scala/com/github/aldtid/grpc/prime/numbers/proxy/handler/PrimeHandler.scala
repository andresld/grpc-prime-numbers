package com.github.aldtid.grpc.prime.numbers.proxy.handler

import cats.Applicative
import cats.data.{EitherT, NonEmptyList}
import cats.effect.Clock
import com.github.aldtid.grpc.prime.numbers.proxy.logging.ProgramLog
import org.typelevel.log4cats.Logger


trait PrimeHandler[F[_]] {

  def handlePrimes(number: Long): EitherT[F, String, NonEmptyList[Int]]

}

object PrimeHandler {

  def handlePrimes[F[_] : Applicative : Clock : Logger, L : ProgramLog](number: Long): EitherT[F, String, NonEmptyList[Int]] =
    EitherT.rightT[F, String](NonEmptyList.of(2, 3, 5, 7))

  def default[F[_] : Applicative : Clock : Logger, L : ProgramLog]: PrimeHandler[F] =
    (number: Long) => handlePrimes(number)

}
