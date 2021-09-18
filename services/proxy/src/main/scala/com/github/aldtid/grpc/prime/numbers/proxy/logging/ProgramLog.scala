package com.github.aldtid.grpc.prime.numbers.proxy.logging

import com.github.aldtid.grpc.prime.numbers.logging.{BaseProgramLog, Loggable}

import org.http4s.{Request, Response}


trait ProgramLog[L] extends BaseProgramLog[L] {

  // Supported http4s types
  implicit def requestLoggable[F[_]]: Loggable[Request[F], L]
  implicit def responseLoggable[F[_]]: Loggable[Response[F], L]

}
