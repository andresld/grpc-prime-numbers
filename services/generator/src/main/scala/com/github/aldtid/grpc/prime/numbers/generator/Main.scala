package com.github.aldtid.grpc.prime.numbers.generator

import cats.effect.{ExitCode, IO, IOApp}


object Main extends IOApp {

  def run(args: List[String]): IO[ExitCode] =
    IO.println("Hello generator!").as(ExitCode.Success)

}
