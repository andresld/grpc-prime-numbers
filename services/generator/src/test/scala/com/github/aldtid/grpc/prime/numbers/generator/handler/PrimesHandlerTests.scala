package com.github.aldtid.grpc.prime.numbers.generator.handler

import com.github.aldtid.grpc.prime.numbers.generator.handler.PrimesHandler._
import com.github.aldtid.grpc.prime.numbers.generator.logging.json.jsonProgramLog
import com.github.aldtid.grpc.prime.numbers.protobuf.primes.{PrimesRequest, PrimesResponse}
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.grpc.Metadata
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger


class PrimesHandlerTests extends AnyFlatSpec with Matchers {

  implicit val logger: Logger[IO] = Slf4jLogger.getLogger

  "isPrime" should "return true if the number is 2" in {

    isPrime(2) shouldBe true

  }

  it should "return false if the number is even" in {

    isPrime(8) shouldBe false

  }

  it should "return true if the number is not even and is prime" in {

    isPrime(11) shouldBe true

  }

  it should "return false if the number is not even and is not prime" in {

    isPrime(12) shouldBe false

  }

  "calculatePrimes" should "calculate all the primes up to passed request number" in {

    calculatePrimes(PrimesRequest(10), new Metadata()).compile.toList.unsafeRunSync() should
      contain theSameElementsInOrderAs List(2, 3, 5, 7).map(PrimesResponse(_))

  }

}
