package com.github.aldtid.grpc.prime.numbers.generator.logging.json

import com.github.aldtid.grpc.prime.numbers.generator.logging.ProgramLog
import com.github.aldtid.grpc.prime.numbers.logging.Loggable
import com.github.aldtid.grpc.prime.numbers.logging.json.JsonBaseProgramLog

import io.circe.Json


trait JsonProgramLog extends ProgramLog[Json] with JsonBaseProgramLog {

  import JsonProgramLog._

}

object JsonProgramLog {

}
