import sbt.ModuleID

val compilerOptions: Seq[String] =
  Seq(
    "-language:higherKinds",
    "-language:implicitConversions",
    "-feature",
    "-deprecation",
    "-unchecked"
  )

val loggingDependencies: Seq[ModuleID] =
  Seq(
    dependencies.cats.core,
    dependencies.circe.generic,
    dependencies.circe.parser,
    dependencies.pureconfig.config,
    dependencies.test.core
  )

val generatorDependencies: Seq[ModuleID] =
  Seq(
    dependencies.cats.core,
    dependencies.cats.effect,
    dependencies.circe.generic,
    dependencies.circe.parser,
    dependencies.grpc.client,
    dependencies.log.binding,
    dependencies.log.slf4j,
    dependencies.pureconfig.config,
    dependencies.test.core
  )

val proxyDependencies: Seq[ModuleID] =
  Seq(
    dependencies.grpc.client,
    dependencies.http4s.client,
    dependencies.http4s.dsl,
    dependencies.http4s.server,
    dependencies.log.binding,
    dependencies.log.slf4j,
    dependencies.pureconfig.config,
    dependencies.test.core
  )

def defineProject(moduleName: String, projectName: String, projectDirectory: String, dependencies: Seq[ModuleID]): Project =
  Project(moduleName, file(projectDirectory))
    .settings(
      // Base definitions
      organization         := "com.github.aldtid",
      name                 := projectName,
      scalaVersion         := "2.13.6",
      version              := "0.1.0-SNAPSHOT",
      scalacOptions       ++= compilerOptions,
      libraryDependencies ++= dependencies,
      // Docker definitions
      Docker / packageName                 := projectName,
      Docker / defaultLinuxInstallLocation := s"/opt/$projectName",
      dockerBaseImage                      := "openjdk:11-jdk-slim",
      dockerLabels                         := Map("version" -> version.value),
      dockerExposedVolumes                 := Seq(
        s"/opt/$projectName/config",
        s"/opt/$projectName/log"
      ),
      // Plugins definitions
      addCompilerPlugin("org.typelevel" % "kind-projector" % "0.13.2" cross CrossVersion.full)
    )
    .enablePlugins(JavaAppPackaging)
    .enablePlugins(DockerPlugin)

lazy val logging = (project in file("services/logging"))
  .settings(
    // Base definitions
    organization         := "com.github.aldtid",
    name                 := "grpc-prime-numbers-logging",
    scalaVersion         := "2.13.6",
    version              := "0.1.0-SNAPSHOT",
    scalacOptions       ++= compilerOptions,
    libraryDependencies ++= loggingDependencies
  )

lazy val protobuf = (project in file("services/protobuf"))
  .settings(
    // Base definitions
    organization   := "com.github.aldtid",
    name           := "grpc-prime-numbers-protobuf",
    scalaVersion   := "2.13.6",
    version        := "0.1.0-SNAPSHOT",
    scalacOptions ++= compilerOptions
  )
  .enablePlugins(Fs2Grpc)

lazy val proxy = defineProject("proxy", "grpc-prime-numbers-proxy", "services/proxy", proxyDependencies)
  .aggregate(logging)
  .dependsOn(logging, protobuf)

lazy val generator = defineProject("generator", "grpc-prime-numbers-generator", "services/generator", generatorDependencies)
  .aggregate(logging)
  .dependsOn(logging, protobuf)

lazy val root = (project in file("."))
  .aggregate(proxy, generator)
