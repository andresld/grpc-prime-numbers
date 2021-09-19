package com.github.aldtid.grpc.prime.numbers.generator.logging

import com.github.aldtid.grpc.prime.numbers.generator.logging.json.jsonProgramLog
import com.github.aldtid.grpc.prime.numbers.protobuf.primes.PrimesRequest

import io.circe.Json
import io.grpc.Metadata
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers


class JsonTests extends AnyFlatSpec with Matchers {

  "jsonProgramJson" should "define Loggable instances with the expected formats" in {

    jsonProgramLog.primesRequestLoggable.format(PrimesRequest(1)) shouldBe
      Json.obj("prime" -> Json.fromInt(1))

    val metadata: Metadata = new Metadata()

    metadata.put(Metadata.Key.of("key1", Metadata.ASCII_STRING_MARSHALLER), "value1")
    metadata.put(Metadata.Key.of("key2", Metadata.ASCII_STRING_MARSHALLER), "value2")

    jsonProgramLog.metadataLoggable.format(metadata) shouldBe
      Json.obj(
        "metadata" -> Json.obj(
          "key1" -> Json.fromString("value1"),
          "key2" -> Json.fromString("value2")
          )
        )

  }

}
