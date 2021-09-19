package com.github.aldtid.grpc.prime.numbers.proxy.handler

import com.github.aldtid.grpc.prime.numbers.protobuf.primes.{PrimesFs2Grpc, PrimesRequest, PrimesResponse}
import com.github.aldtid.grpc.prime.numbers.proxy.handler.PrimesHandler._
import com.github.aldtid.grpc.prime.numbers.proxy.logging.json.jsonProgramLog

import cats.data.NonEmptyList
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import fs2.Stream
import io.grpc.Metadata
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger


class PrimesHandlerTests extends AnyFlatSpec with Matchers {

  implicit val logger: Logger[IO] = Slf4jLogger.getLogger

  "handlePrimes" should "perform a request to extract the prime numbers and return them in a list" in {

    val service: PrimesFs2Grpc[IO, Metadata] = (request: PrimesRequest, ctx: Metadata) =>
      if (request.number == 10) Stream(2, 3, 5, 7).map(PrimesResponse(_)) else Stream()

    handlePrimes(10, service).value.unsafeRunSync() shouldBe Right(NonEmptyList.of(2, 3, 5, 7))

  }

  it should "perform a request to extract the prime numbers and return an error as the list is empty" in {

    val service: PrimesFs2Grpc[IO, Metadata] = (request: PrimesRequest, ctx: Metadata) =>
      if (request.number == 10) Stream() else Stream(2, 3, 5, 7).map(PrimesResponse(_))

    handlePrimes(10, service).value.unsafeRunSync() shouldBe Left("no primes")

  }

}
