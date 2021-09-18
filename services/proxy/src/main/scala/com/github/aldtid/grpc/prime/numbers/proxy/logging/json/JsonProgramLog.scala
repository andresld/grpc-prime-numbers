package com.github.aldtid.grpc.prime.numbers.proxy.logging.json

import com.github.aldtid.grpc.prime.numbers.logging.Loggable
import com.github.aldtid.grpc.prime.numbers.logging.json.JsonBaseProgramLog
import com.github.aldtid.grpc.prime.numbers.proxy.logging.ProgramLog

import io.circe.Json
import org.http4s.{Header, Headers, Request, Response}


trait JsonProgramLog extends ProgramLog[Json] with JsonBaseProgramLog {

  import JsonProgramLog._

  implicit def requestLoggable[F[_]]: Loggable[Request[F], Json] = jsonRequestLoggable
  implicit def responseLoggable[F[_]]: Loggable[Response[F], Json] = jsonResponseLoggable

}

object JsonProgramLog {

  // ----- LOGGABLE INSTANCES -----
  def jsonRequestLoggable[F[_]]: Loggable[Request[F], Json] = request =>
    Json.obj(
      "request" -> Json.obj(
        "method" -> Json.fromString(request.method.name),
        "uri" -> Json.fromString(request.uri.renderString),
        "version" -> Json.fromString(request.httpVersion.renderString),
        "headers" -> toJson(request.headers)
      )
    )

  def jsonResponseLoggable[F[_]]: Loggable[Response[F], Json] = response =>
    Json.obj(
      "response"-> Json.obj(
        "status" -> Json.fromInt(response.status.code),
        "version"-> Json.fromString(response.httpVersion.renderString),
        "headers" -> toJson(response.headers)
      )
    )
  // ----------

  // ----- UTILITY FUNCTIONS -----
  def toJson(headers: Headers): Json = headers.headers.map(toJson).foldLeft(Json.arr())(_ deepMerge _)

  def toJson(raw: Header.Raw): Json = Json.obj(raw.name.toString -> Json.fromString(raw.value))
  // ----------

}
