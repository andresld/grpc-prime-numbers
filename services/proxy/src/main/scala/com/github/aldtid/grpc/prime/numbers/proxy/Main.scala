package com.github.aldtid.grpc.prime.numbers.proxy

import cats.effect.{ExitCode, IO, IOApp}


object Main extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    IO.println("Hello proxy!").as(ExitCode.Success)

}
