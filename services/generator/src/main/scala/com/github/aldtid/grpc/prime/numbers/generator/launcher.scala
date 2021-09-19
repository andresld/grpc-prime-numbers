package com.github.aldtid.grpc.prime.numbers.generator

import com.github.aldtid.grpc.prime.numbers.generator.configuration._
import com.github.aldtid.grpc.prime.numbers.generator.handler.PrimesHandler
import com.github.aldtid.grpc.prime.numbers.generator.logging.ProgramLog
import com.github.aldtid.grpc.prime.numbers.generator.logging.messages._
import com.github.aldtid.grpc.prime.numbers.generator.logging.tags.launcherTag
import com.github.aldtid.grpc.prime.numbers.logging.Log
import com.github.aldtid.grpc.prime.numbers.logging.implicits.all._
import com.github.aldtid.grpc.prime.numbers.protobuf.primes.PrimesFs2Grpc

import cats.effect.{Async, ExitCode, Resource, Sync}
import cats.implicits._
import fs2.grpc.syntax.all.fs2GrpcSyntaxServerBuilder
import io.grpc.ServerServiceDefinition
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder
import org.typelevel.log4cats.Logger
import pureconfig.error.ConfigReaderFailures

import java.net.InetSocketAddress
import java.util.concurrent.{ExecutorService, Executors}

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
   * Creates a resource for to handle primes service requests.
   *
   * @tparam F context type
   * @tparam L logging type to format
   * @return a resource to use the prime service definition
   */
  def primesService[F[_] : Async : Logger, L: ProgramLog]: Resource[F, ServerServiceDefinition] =
    PrimesFs2Grpc.bindServiceResource[F](PrimesHandler.default)

  /**
   * Starts a gRPC server for passed service that runs forever.
   *
   * @param es execution service to handle the requests on
   * @param server server configuration
   * @param service service to expose
   * @tparam F context type
   * @return the server exit code
   */
  def startServer[F[_] : Async](es: ExecutorService, server: Server)(service: ServerServiceDefinition): F[ExitCode] =
    NettyServerBuilder
      .forAddress(new InetSocketAddress(server.host, server.port))
      .executor(es)
      .addService(service)
      .resource[F]
      .evalMap(server => Async[F].delay(server.start()))
      .use(_ => Async[F].never[ExitCode])

  /**
   * Prepares the environment to run the server and starts it.
   *
   * The environment steps to prepare before the server run are:
   *   - the configuration load
   *   - thread pools creation
   *   - services definition
   *
   * If the configuration load fails, the application will not be started.
   *
   * @tparam F context type
   * @tparam L logging type to format
   * @return the exit code for the server
   */
  def prepareAndStart[F[_] : Async : Logger, L](implicit pl: ProgramLog[L]): F[ExitCode] = {

    import pl._

    def threadPoolsAndRun(configuration: Configuration): F[ExitCode] =
      for {

        _    <- Logger[F].info(serverThreadPool |+| configuration.threadPools.server.asThreadPool |+| launcherTag)
        ec   <- threadPool(configuration.threadPools.server)
        _    <- Logger[F].info(startingServer |+| launcherTag)
        code <- primesService.use(startServer[F](ec, configuration.server))

      } yield code

    handleConfiguration(loadConfiguration, threadPoolsAndRun)

  }

}
