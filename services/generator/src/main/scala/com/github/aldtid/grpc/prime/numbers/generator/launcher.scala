package com.github.aldtid.grpc.prime.numbers.generator

import com.github.aldtid.grpc.prime.numbers.generator.logging.ProgramLog
import com.github.aldtid.grpc.prime.numbers.generator.logging.messages._
import com.github.aldtid.grpc.prime.numbers.generator.logging.tags.launcherTag
import com.github.aldtid.grpc.prime.numbers.logging.implicits.all._
import com.github.aldtid.grpc.prime.numbers.protobuf.primes.PrimesFs2Grpc

import cats.Monad
import cats.effect.{Async, ExitCode, Resource}
import cats.implicits._
import fs2.grpc.syntax.all.fs2GrpcSyntaxServerBuilder
import io.grpc.ServerServiceDefinition
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder
import org.typelevel.log4cats.Logger


object launcher {

  def primesService[F[_] : Async, L: ProgramLog]: Resource[F, ServerServiceDefinition] =
    PrimesFs2Grpc.bindServiceResource[F](PrimesHandler.default)

  def startServer[F[_] : Async](service: ServerServiceDefinition): F[ExitCode] =
    NettyServerBuilder
      .forPort(9999)
      .addService(service)
      .resource[F]
      .evalMap(server => Async[F].delay(server.start()))
      .use(_ => Async[F].never[ExitCode])

  def start[F[_] : Async : Monad : Logger, L](implicit pl: ProgramLog[L]): F[ExitCode] = {

    import pl._

    for {
      _    <- Logger[F].info(startingServer |+| launcherTag)
      code <- primesService.use(startServer[F])
    } yield code

  }

}
