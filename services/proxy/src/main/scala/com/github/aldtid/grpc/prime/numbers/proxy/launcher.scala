package com.github.aldtid.grpc.prime.numbers.proxy

import com.github.aldtid.grpc.prime.numbers.logging.Log
import com.github.aldtid.grpc.prime.numbers.logging.implicits.all._
import com.github.aldtid.grpc.prime.numbers.proxy.configuration._
import com.github.aldtid.grpc.prime.numbers.proxy.handler.PrimesHandler
import com.github.aldtid.grpc.prime.numbers.proxy.logging.ProgramLog
import com.github.aldtid.grpc.prime.numbers.proxy.logging.messages._
import com.github.aldtid.grpc.prime.numbers.proxy.logging.tags.launcherTag
import com.github.aldtid.grpc.prime.numbers.protobuf.primes.PrimesFs2Grpc

import cats.effect.{ExitCode, Resource, Sync}
import cats.effect.kernel.Async
import cats.implicits._
import fs2.grpc.syntax.all.fs2GrpcSyntaxManagedChannelBuilder
import io.grpc.ManagedChannel
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.dsl.Http4sDsl
import org.typelevel.log4cats.Logger
import pureconfig.error.ConfigReaderFailures

import java.util.concurrent.Executors

import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService}


object launcher {

  /**
   * Tries to load a configuration from the file system using passed effect.
   *
   * In case the load succeeds, a function is applied for parsed configuration, otherwise the exit code is a failure.
   * Logs are shown before and after loading the configuration.
   *
   * @param eitherF effect for configuration load result
   * @param onSuccess function to apply if the load succeeds
   * @param pl logging instances
   * @tparam F context type
   * @tparam L logging type to format
   * @return the resulting exit code of applying passed function or a failed configuration load
   */
  def handleConfiguration[F[_] : Sync : Logger, L](eitherF: F[Either[ConfigReaderFailures, Configuration]],
                                                   onSuccess: Configuration => F[ExitCode])
                                                  (implicit pl: ProgramLog[L]): F[ExitCode] = {

    import pl._

    val baseLog: Log[L] = launcherTag

    Logger[F].info(baseLog |+| loadingConfiguration) *> eitherF.flatMap({

      case Right(configuration) => Logger[F].info(baseLog |+| configurationLoaded) *> onSuccess(configuration)
      case Left(errors)         => Logger[F].info(baseLog |+| configurationErrors |+| errors).as(ExitCode.Error)

    })

  }

  /**
   * Creates a fixed thread pool with passed size.
   *
   * @param size thread pool size
   * @tparam F context type
   * @return the thread pool
   */
  def threadPool[F[_]: Sync](size: Int): F[ExecutionContextExecutorService] =
    Sync[F].delay(ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(size)))

  /**
   * Creates a ManagedChannel instance to tardet gRPC server.
   *
   * @param grpc gRPC configuration
   * @tparam F context type
   * @return a resource to use the created managed channel
   */
  def managedChannelResource[F[_] : Async](grpc: GRPC): Resource[F, ManagedChannel] =
    NettyChannelBuilder
      .forAddress(grpc.host, grpc.port)
      .usePlaintext()
      .resource[F]

  /**
   * Prepares the environment to run the server and starts it.
   *
   * The environment steps to prepare before the server run are:
   *   - the configuration load
   *   - thread pools creation
   *   - endpoint handler
   *   - a gRPC client to perform requests
   *
   * If the configuration load fails, the application will not be started.
   *
   * @tparam F context type
   * @tparam L logging type to format
   * @return the exit code for the server
   */
  def prepareAndStart[F[_] : Async : Http4sDsl : Logger, L](implicit pl: ProgramLog[L]): F[ExitCode] = {

    import pl._

    def threadPoolsAndRun(configuration: Configuration): F[ExitCode] =
      for {

        _    <- Logger[F].info(serverThreadPool |+| configuration.threadPools.server.asThreadPool |+| launcherTag)
        ec   <- threadPool(configuration.threadPools.server)
        code <- run(configuration, ec)

      } yield code

    def run(configuration: Configuration, ec: ExecutionContext): F[ExitCode] =
      handlerAndStart(configuration.grpc, startServer(ec, configuration.server))

    def handlerAndStart(grpc: GRPC, f: PrimesHandler[F] => F[ExitCode]): F[ExitCode] =
      Logger[F].info(creatingPrimesClient |+| launcherTag) *>
        managedChannelResource(grpc)
          .flatMap(PrimesFs2Grpc.stubResource[F])
          .use(primes => f(PrimesHandler.default[F, L](primes)))

    def startServer(ec: ExecutionContext, server: Server)(handler: PrimesHandler[F]): F[ExitCode] =
      Logger[F].info(startingServer |+| launcherTag) *> start(ec, server, handler)

    handleConfiguration(loadConfiguration, threadPoolsAndRun)

  }

  /**
   * Starts a server with passed thread pool, server configuration and endpoints handler.
   *
   * @param ec server thread pool
   * @param server server configuration
   * @param handler primes endpoints handler
   * @tparam F context type
   * @tparam L logging type to format
   * @return the exit code for the server
   */
  def start[F[_] : Async : Http4sDsl : Logger, L : ProgramLog](ec: ExecutionContext,
                                                               server: Server,
                                                               handler: PrimesHandler[F]): F[ExitCode] =
    BlazeServerBuilder[F](ec)
      .bindHttp(server.port, server.host)
      .withHttpApp(application.app(server.basePath, handler))
      .serve
      .compile
      .drain
      .as(ExitCode.Success)

}
