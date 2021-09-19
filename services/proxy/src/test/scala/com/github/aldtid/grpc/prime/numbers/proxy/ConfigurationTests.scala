package com.github.aldtid.grpc.prime.numbers.proxy

import com.github.aldtid.grpc.prime.numbers.proxy.configuration._
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import org.http4s.implicits.http4sLiteralsSyntax
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers


class ConfigurationTests extends AnyFlatSpec with Matchers {

  "loadConfiguration" should "correctly load a configuration from the file system" in {

    loadConfiguration[IO].unsafeRunSync() shouldBe
      Right(
        Configuration(
          Server("localhost", 8080, "/"),
          Client("localhost", 9999),
          ThreadPools(10)
        )
      )

  }

}
