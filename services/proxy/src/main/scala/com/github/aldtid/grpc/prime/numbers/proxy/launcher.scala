package com.github.aldtid.grpc.prime.numbers.proxy


import com.github.aldtid.grpc.prime.numbers.proxy.handler.PrimeHandler
import com.github.aldtid.grpc.prime.numbers.proxy.logging.ProgramLog

import cats.effect.ExitCode
import cats.effect.kernel.Async
import cats.implicits._
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.dsl.Http4sDsl
import org.typelevel.log4cats.Logger

import scala.concurrent.ExecutionContext


object launcher {

  def start[F[_] : Async : Http4sDsl : Logger, L : ProgramLog](ec: ExecutionContext): F[ExitCode] =

    BlazeServerBuilder[F](ec)
      .bindHttp(8080, "localhost")
      .withHttpApp(application.app(PrimeHandler.default[F, L]))
      .serve
      .compile
      .drain
      .as(ExitCode.Success)

}
