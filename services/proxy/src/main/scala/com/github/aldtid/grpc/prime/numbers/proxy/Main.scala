package com.github.aldtid.grpc.prime.numbers.proxy

import com.github.aldtid.grpc.prime.numbers.proxy.launcher.prepareAndStart
import com.github.aldtid.grpc.prime.numbers.proxy.logging.json.jsonProgramLog

import cats.effect.{ExitCode, IO, IOApp}
import io.circe.Json
import org.http4s.dsl.{Http4sDsl, io}
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger


object Main extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    for {
      logger <- Slf4jLogger.create[IO]
      code   <- run(io, logger)
    } yield code

  def run(implicit dsl: Http4sDsl[IO], logger: Logger[IO]): IO[ExitCode] =
    prepareAndStart[IO, Json]

}
