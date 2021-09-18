package com.github.aldtid.grpc.prime.numbers.logging

import com.github.aldtid.grpc.prime.numbers.logging.Log.createLog
import com.github.aldtid.grpc.prime.numbers.logging.model._

import io.circe.{Json, Printer}
import io.circe.generic.auto._
import io.circe.syntax._
import pureconfig.error.ConfigReaderFailures


object json {

  // ----- BASE LOG INSTANCE -----
  val jsonLog: Log[Json] = createLog[Json](Json.obj(), _.printWith(Printer.noSpaces))((x, y) => y deepMerge x)
  // ----------

  // ----- LOGGABLE INSTANCES -----
  val jsonConfigReaderFailuresLoggable: Loggable[ConfigReaderFailures, Json] =
    failures => Json.obj("failures" -> failures.prettyPrint().filterNot(_ == '\r').asJson)

  val jsonMessageLoggable: Loggable[Message, Json] = _.asJson
  val jsonTagLoggable: Loggable[Tag, Json] = _.asJson
  val jsonUsernameLoggable: Loggable[Username, Json] = _.asJson
  val jsonIdentifierLoggable: Loggable[Identifier, Json] = _.asJson
  val jsonLatencyLoggable: Loggable[Latency, Json] = _.asJson
  val jsonThreadPoolLoggable: Loggable[ThreadPool, Json] = _.asJson
  // ----------

  implicit val jsonBaseProgramLog: BaseProgramLog[Json] = new BaseProgramLog[Json] {

    implicit val log: Log[Json] = jsonLog

    implicit val configReaderFailuresLoggable: Loggable[ConfigReaderFailures, Json] = jsonConfigReaderFailuresLoggable

    implicit val messageLoggable: Loggable[Message, Json] = jsonMessageLoggable
    implicit val tagLoggable: Loggable[Tag, Json] = jsonTagLoggable
    implicit val usernameLoggable: Loggable[Username, Json] = jsonUsernameLoggable
    implicit val identifierLoggable: Loggable[Identifier, Json] = jsonIdentifierLoggable
    implicit val latencyLoggable: Loggable[Latency, Json] = jsonLatencyLoggable
    implicit val threadPoolLoggable: Loggable[ThreadPool, Json] = jsonThreadPoolLoggable

  }

}
