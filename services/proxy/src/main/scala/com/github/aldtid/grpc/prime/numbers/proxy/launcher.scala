package com.github.aldtid.grpc.prime.numbers.proxy

import com.github.aldtid.grpc.prime.numbers.logging.implicits.all._
import com.github.aldtid.grpc.prime.numbers.proxy.handler.PrimesHandler
import com.github.aldtid.grpc.prime.numbers.proxy.logging.ProgramLog
import com.github.aldtid.grpc.prime.numbers.proxy.logging.messages._
import com.github.aldtid.grpc.prime.numbers.proxy.logging.tags.launcherTag
import com.github.aldtid.grpc.prime.numbers.protobuf.primes.PrimesFs2Grpc

import cats.effect.{ExitCode, Resource}
import cats.effect.kernel.Async
import cats.implicits._
import fs2.grpc.syntax.all.fs2GrpcSyntaxManagedChannelBuilder
import io.grpc.ManagedChannel
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.dsl.Http4sDsl
import org.typelevel.log4cats.Logger

import scala.concurrent.ExecutionContext


object launcher {

  def managedChannelResource[F[_] : Async]: Resource[F, ManagedChannel] =
    NettyChannelBuilder
      .forAddress("127.0.0.1", 9999)
      .usePlaintext()
      .resource[F]

  def prepareAndStart[F[_] : Async : Http4sDsl : Logger, L](ec: ExecutionContext)
                                                           (implicit pl: ProgramLog[L]): F[ExitCode] = {

    import pl._

    def startServer(handler: PrimesHandler[F]): F[ExitCode] =
      Logger[F].info(startingServer |+| launcherTag) *> start(ec, handler)

    def handle(f: PrimesHandler[F] => F[ExitCode]): F[ExitCode] =
      Logger[F].info(creatingPrimesClient |+| launcherTag) *>
        managedChannelResource
          .flatMap(PrimesFs2Grpc.stubResource[F])
          .use(primes => f(PrimesHandler.default[F, L](primes)))

    handle(startServer)

  }

  def start[F[_] : Async : Http4sDsl : Logger, L : ProgramLog](ec: ExecutionContext,
                                                               handler: PrimesHandler[F]): F[ExitCode] =

    BlazeServerBuilder[F](ec)
      .bindHttp(8080, "localhost")
      .withHttpApp(application.app(handler))
      .serve
      .compile
      .drain
      .as(ExitCode.Success)

}
