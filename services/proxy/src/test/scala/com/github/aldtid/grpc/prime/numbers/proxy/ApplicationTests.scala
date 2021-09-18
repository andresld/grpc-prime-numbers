package com.github.aldtid.grpc.prime.numbers.proxy

import com.github.aldtid.grpc.prime.numbers.proxy.application._
import com.github.aldtid.grpc.prime.numbers.proxy.handler.PrimeHandler
import com.github.aldtid.grpc.prime.numbers.proxy.logging.json.jsonProgramLog

import cats.Id
import cats.data.{EitherT, NonEmptyList}
import io.circe.Json
import org.http4s.{Charset, Headers, MediaType, Request, Response, Status}
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.{`Content-Length`, `Content-Type`}
import org.http4s.implicits.http4sLiteralsSyntax
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers


class ApplicationTests extends AnyFlatSpec with Matchers {

  "developers" should "return the expected Ok status if no errors happened" in {

    implicit val dsl: Http4sDsl[Id] = new Http4sDsl[Id] {}

    val handler: PrimeHandler[Id] = number =>
      if (number == 10) EitherT.rightT[Id, String](NonEmptyList.of(2, 3, 5, 7))
      else EitherT.leftT[Id, NonEmptyList[Int]]("error!")

    val body: String = "2,3,5,7"
    val headers: Headers = Headers(`Content-Type`(MediaType.text.plain, Charset.`UTF-8`), `Content-Length`(body.length))
    val response: Response[Id] = developers[Id, Json](handler).apply(Request(uri = uri"/prime/10"))

    response.status shouldBe Status.Ok
    response.headers shouldBe headers
    response.body.compile.toList shouldBe body.getBytes

  }

  it should "return the expected BadRequest status if at least an error happens" in {

    implicit val dsl: Http4sDsl[Id] = new Http4sDsl[Id] {}

    val handler: PrimeHandler[Id] = _ => EitherT.leftT[Id, NonEmptyList[Int]]("error!")

    val body: String = "error!"
    val headers: Headers = Headers(`Content-Type`(MediaType.text.plain, Charset.`UTF-8`), `Content-Length`(body.length))
    val response: Response[Id] = developers[Id, Json](handler).apply(Request(uri = uri"/prime/10"))

    response.status shouldBe Status.BadRequest
    response.headers shouldBe headers
    response.body.compile.toList shouldBe body.getBytes

  }

  "notFound" should "return a function that always returns a NotFound response" in {

    implicit val dsl: Http4sDsl[Id] = new Http4sDsl[Id] {}

    val body: String = "missing resource"
    val headers: Headers = Headers(`Content-Type`(MediaType.text.plain, Charset.`UTF-8`), `Content-Length`(body.length))
    val response: Response[Id] = notFound[Id].apply(Request())

    response.status shouldBe Status.NotFound
    response.headers shouldBe headers
    response.body.compile.toList shouldBe body.getBytes

  }

}
