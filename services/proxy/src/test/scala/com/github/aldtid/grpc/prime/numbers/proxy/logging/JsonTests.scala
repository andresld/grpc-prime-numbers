package com.github.aldtid.grpc.prime.numbers.proxy.logging

import com.github.aldtid.grpc.prime.numbers.proxy.logging.json.jsonProgramLog
import com.github.aldtid.grpc.prime.numbers.protobuf.primes.PrimesRequest

import cats.Id
import io.circe.Json
import org.http4s.Header.Raw
import org.http4s.{Headers, Request, Response}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.typelevel.ci.CIString


class JsonTests extends AnyFlatSpec with Matchers {

  "jsonProgramLog" should "define Loggable instances with the expected formats" in {

    val request: Request[Id] = Request(headers = Headers(Raw(CIString("header"), "value")))
    val response: Response[Id] = Response(headers = Headers(Raw(CIString("header"), "value")))

    jsonProgramLog.requestLoggable.format(request) shouldBe
      Json.obj(
        "request" -> Json.obj(
          "method" -> Json.fromString("GET"),
          "uri" -> Json.fromString("/"),
          "version" -> Json.fromString("HTTP/1.1"),
          "headers" -> Json.obj(
            "header" -> Json.fromString("value")
          )
        )
      )

    jsonProgramLog.responseLoggable.format(response) shouldBe
      Json.obj(
        "response" -> Json.obj(
          "status" -> Json.fromInt(200),
          "version" -> Json.fromString("HTTP/1.1"),
          "headers" -> Json.obj(
            "header" -> Json.fromString("value")
          )
        )
      )

    jsonProgramLog.primesRequestLoggable.format(PrimesRequest(1)) shouldBe
      Json.obj("prime" -> Json.fromInt(1))

  }

}
