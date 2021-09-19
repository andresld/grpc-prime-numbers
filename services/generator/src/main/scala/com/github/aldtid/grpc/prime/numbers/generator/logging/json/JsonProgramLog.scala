package com.github.aldtid.grpc.prime.numbers.generator.logging.json

import com.github.aldtid.grpc.prime.numbers.generator.logging.ProgramLog
import com.github.aldtid.grpc.prime.numbers.logging.Loggable
import com.github.aldtid.grpc.prime.numbers.logging.json.JsonBaseProgramLog
import com.github.aldtid.grpc.prime.numbers.protobuf.primes.PrimesRequest

import io.circe.Json
import io.circe.syntax._
import io.grpc.Metadata

import scala.jdk.CollectionConverters._


trait JsonProgramLog extends ProgramLog[Json] with JsonBaseProgramLog {

  import JsonProgramLog._

  implicit val primesRequestLoggable: Loggable[PrimesRequest, Json] = jsonPrimesRequestLoggable
  implicit val metadataLoggable: Loggable[Metadata, Json] = jsonMetadataLoggable

}

object JsonProgramLog {

  val jsonPrimesRequestLoggable: Loggable[PrimesRequest, Json] = request =>
    Json.obj("prime" -> request.number.asJson)

  val jsonMetadataLoggable: Loggable[Metadata, Json] = metadata =>
    Json.obj("metadata" -> toJson(metadata))

  def toJson(metadata: Metadata): Json =
    metadata.keys().asScala.foldLeft(Json.obj())((json, key) =>
      json.deepMerge(Json.obj(key -> metadata.get(Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER)).asJson))
    )

}
