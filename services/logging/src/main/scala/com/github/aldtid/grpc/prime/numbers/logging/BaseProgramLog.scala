package com.github.aldtid.grpc.prime.numbers.logging

import com.github.aldtid.grpc.prime.numbers.logging.model._

import pureconfig.error.ConfigReaderFailures

/**
 * Representation of the base generic logging system for a program environment.
 *
 * Any implementation for this representation should add a Log instance, to combine the different logs, and multiple
 * Loggable instances, one per type to log, which will represent the later logging representation for that structure
 * according to L type format.
 *
 * @tparam L type to format the logs
 */
trait BaseProgramLog[L] {

  // Logging instance
  implicit val log: Log[L]

  // Supported pureconfig types
  implicit val configReaderFailuresLoggable: Loggable[ConfigReaderFailures, L]

  // Supported common model types
  implicit val messageLoggable: Loggable[Message, L]
  implicit val tagLoggable: Loggable[Tag, L]
  implicit val usernameLoggable: Loggable[Username, L]
  implicit val identifierLoggable: Loggable[Identifier, L]
  implicit val latencyLoggable: Loggable[Latency, L]
  implicit val threadPoolLoggable: Loggable[ThreadPool, L]

}
