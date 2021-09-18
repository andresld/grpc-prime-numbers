package com.github.aldtid.grpc.prime.numbers.logging

import com.github.aldtid.grpc.prime.numbers.logging.json.jsonBaseProgramLog
import com.github.aldtid.grpc.prime.numbers.logging.model._

import io.circe.Json
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import pureconfig.error.{CannotConvert, ConfigReaderFailures, ConvertFailure}


class JsonTests extends AnyFlatSpec with Matchers {

  "jsonProgrammingLog" should "define a Log[Json] instance with the expected behaviour" in {

    import jsonBaseProgramLog.{latencyLoggable, messageLoggable}

    val log: Log[Json] = jsonBaseProgramLog.log

    log.value shouldBe Json.obj()
    log.formatted shouldBe "{}"

    val value1: Log[Json] = log |+| Latency(1)

    value1.value shouldBe Json.obj("latency" -> Json.fromLong(1))
    value1.formatted shouldBe """{"latency":1}"""

    val value2: Log[Json] = value1 |+| Message("test")

    value2.value shouldBe Json.obj("latency" -> Json.fromLong(1), "message"-> Json.fromString("test"))
    value2.formatted shouldBe """{"latency":1,"message":"test"}"""

  }

  it should "define Loggable instances with the expected formats" in {

    jsonBaseProgramLog.configReaderFailuresLoggable
      .format(ConfigReaderFailures(ConvertFailure(CannotConvert("value", "type", "reason"), None, "key"))) shouldBe
        Json.obj("failures" -> Json.fromString("at 'key':\n  - Cannot convert 'value' to type: reason."))

    jsonBaseProgramLog.messageLoggable.format(Message("test")) shouldBe Json.obj("message" -> Json.fromString("test"))
    jsonBaseProgramLog.tagLoggable.format(Tag("test")) shouldBe Json.obj("tag" -> Json.fromString("test"))
    jsonBaseProgramLog.usernameLoggable.format(Username("test")) shouldBe Json.obj("username" -> Json.fromString("test"))
    jsonBaseProgramLog.identifierLoggable.format(Identifier("test")) shouldBe Json.obj("id" -> Json.fromString("test"))
    jsonBaseProgramLog.latencyLoggable.format(Latency(1)) shouldBe Json.obj("latency" -> Json.fromInt(1))
    jsonBaseProgramLog.threadPoolLoggable.format(ThreadPool(1)) shouldBe Json.obj("threadPool" -> Json.fromInt(1))

  }

}
