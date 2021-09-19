package com.github.aldtid.grpc.prime.numbers.generator

import com.github.aldtid.grpc.prime.numbers.generator.launcher.prepareAndStart
import com.github.aldtid.grpc.prime.numbers.generator.logging.json.jsonProgramLog

import cats.effect.{ExitCode, IO, IOApp}
import io.circe.Json
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger


object Main extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    for {
      logger <- Slf4jLogger.create[IO]
      code   <- run(logger)
    } yield code

  def run(implicit logger: Logger[IO]): IO[ExitCode] =
    prepareAndStart[IO, Json]

}
