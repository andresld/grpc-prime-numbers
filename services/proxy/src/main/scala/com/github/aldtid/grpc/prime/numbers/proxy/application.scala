package com.github.aldtid.grpc.prime.numbers.proxy

import com.github.aldtid.grpc.prime.numbers.logging.implicits.all._
import com.github.aldtid.grpc.prime.numbers.proxy.handler.PrimesHandler
import com.github.aldtid.grpc.prime.numbers.proxy.logging.ProgramLog
import com.github.aldtid.grpc.prime.numbers.proxy.logging.messages._
import com.github.aldtid.grpc.prime.numbers.proxy.logging.tags.routerTag
import cats.Monad
import cats.effect.{Clock, Sync}
import cats.implicits._
import org.http4s.{HttpApp, HttpRoutes, Request, Response}
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.typelevel.log4cats.Logger


object application {

  /**
   * Main application routes instance.
   *
   * Composes all the application routes, logs the requests and responses and processes the incoming requests.
   *
   * @param basePath API base path
   * @param handler 'primes' endpoints controller
   * @param pl logging instances
   * @tparam F context
   * @tparam L logging type to format
   * @return an application that handles every API route
   */
  def app[F[_] : Sync : Clock : Logger : Http4sDsl, L](basePath: String,
                                                       handler: PrimesHandler[F])
                                                      (implicit pl: ProgramLog[L]): HttpApp[F] = {

    import pl._

    val routes: HttpRoutes[F] = Router(basePath -> HttpRoutes.of(developers(handler)))

    val process: Function[Request[F], F[Response[F]]] =
      request => routes.apply(request).getOrElseF(notFound[F].apply(request))

    HttpApp[F](request =>

      for {

        start    <- Clock[F].realTime
        _        <- Logger[F].info(incomingRequest |+| request |+| routerTag)

        response <- process(request)

        end      <- Clock[F].realTime
        latency   = (end - start).toMillis.asLatency
        _        <- Logger[F].info(outgoingResponse |+| response |+| latency |+| routerTag)

      } yield response

    )

  }

  /**
   * Defines the routes for 'prime' endpoints group.
   *
   * All the endpoints defined are managed by passed PrimeHandler instance. As a partial function, no other
   * that 'prime' endpoints will be managed by this function.
   *
   * @param handler handler functions for each endpoint
   * @param dsl routes dsl
   * @tparam F context
   * @tparam L logging type to format
   * @return a partial function that handles each of 'developers' endpoints
   */
  def developers[F[_] : Monad, L : ProgramLog](handler: PrimesHandler[F])
                                              (implicit dsl: Http4sDsl[F]): PartialFunction[Request[F], F[Response[F]]] = {

    import dsl._

    {

      case GET -> Root / "prime" / IntVar(prime) =>
        handler.handlePrimes(prime)
          .bimap(BadRequest(_), list => Ok(list.mkString_(",")))
          .foldF(identity, identity)

    }

  }

  /**
   * Fallback function for when a route does not match with any of previous routes.
   *
   * This function replaces [[org.http4s.syntax.KleisliResponseOps.orNotFound]] syntax, defining a custom response for
   * routes that do not match with exposed API.
   *
   * @param dsl routes dsl
   * @tparam F context
   * @return a function that always returns a not found error with a custom body
   */
  def notFound[F[_] : Monad](implicit dsl: Http4sDsl[F]): Request[F] => F[Response[F]] = {

    import dsl._

    _ => NotFound("missing resource")

  }

}
