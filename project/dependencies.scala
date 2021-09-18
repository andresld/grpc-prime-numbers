import sbt._


object dependencies {

  lazy val version = new {
    val http4s: String = "0.23.1"
    val logback: String = "1.2.5"
    val pureconfig: String = "0.16.0"
    val scalatest: String = "3.2.9"
    val slf4j: String = "2.1.1"
  }

  lazy val grpc = new {
    val client: ModuleID = "io.grpc" % "grpc-netty-shaded" % scalapb.compiler.Version.grpcJavaVersion
  }

  lazy val http4s = new {
    val client: ModuleID = "org.http4s" %% "http4s-blaze-client" % version.http4s % Test
    val dsl: ModuleID = "org.http4s" %% "http4s-dsl" % version.http4s
    val server: ModuleID = "org.http4s" %% "http4s-blaze-server" % version.http4s
  }

  lazy val log = new {
    val slf4j: ModuleID = "org.typelevel" %% "log4cats-slf4j" % version.slf4j
    val binding: ModuleID = "ch.qos.logback" % "logback-classic" % version.logback
  }

  lazy val pureconfig = new {
    val config: ModuleID = "com.github.pureconfig" %% "pureconfig" % version.pureconfig
  }

  lazy val test = new  {
    val core: ModuleID = "org.scalatest" %% "scalatest" % version.scalatest % Test
  }
  
}
