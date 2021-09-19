package com.github.aldtid.grpc.prime.numbers.proxy

import cats.effect.Sync
import pureconfig._
import pureconfig.error.ConfigReaderFailures
import pureconfig.generic.auto._


object configuration {

  final case class Configuration(server: Server, grpc: GRPC, threadPools: ThreadPools)

  final case class Server(host: String, port: Int, basePath: String)

  final case class GRPC(host: String, port: Int)

  final case class ThreadPools(server: Int)

  /**
   * Reads a configuration file from the file system and tries to parse it.
   *
   * @tparam F context type
   * @return the loaded configuration or a reading failure
   */
  def loadConfiguration[F[_] : Sync]: F[Either[ConfigReaderFailures, Configuration]] =
    Sync[F].delay(ConfigSource.default.load[Configuration])

}
