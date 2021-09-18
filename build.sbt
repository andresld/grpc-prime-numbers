import sbt.ModuleID

val compilerOptions: Seq[String] =
  Seq(
    "-language:higherKinds",
    "-language:implicitConversions",
    "-feature",
    "-deprecation",
    "-unchecked"
  )

val libDependencies: Seq[ModuleID] =
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

def defineProject(moduleName: String, projectName: String, projectDirectory: String): Project =
  Project(moduleName, file(projectDirectory))
    .settings(
      // Base definitions
      organization         := "com.github.aldtid",
      name                 := projectName,
      scalaVersion         := "2.13.6",
      version              := "0.1.0-SNAPSHOT",
      scalacOptions       ++= compilerOptions,
      libraryDependencies ++= libDependencies,
      // Docker definitions
      Docker / packageName                 := projectName,
      Docker / defaultLinuxInstallLocation := s"/opt/$projectName",
      dockerBaseImage                      := "openjdk:11-jdk-slim",
      dockerLabels                         := Map("version" -> version.value),
      dockerExposedPorts                   += 8080,
      dockerExposedVolumes                 := Seq(
        s"/opt/$projectName/config",
        s"/opt/$projectName/log"
      ),
      // Plugins definitions
      addCompilerPlugin("org.typelevel" % "kind-projector" % "0.13.2" cross CrossVersion.full)
    )
    .enablePlugins(JavaAppPackaging)
    .enablePlugins(DockerPlugin)
    .enablePlugins(Fs2Grpc)

lazy val logging = (project in file("services/logging"))
  .settings(
    // Base definitions
    organization         := "com.github.aldtid",
    name                 := "grpc-prime-numbers-logging",
    scalaVersion         := "2.13.6",
    version              := "0.1.0-SNAPSHOT",
    scalacOptions       ++= compilerOptions,
    libraryDependencies ++= Seq(
      dependencies.cats.core,
      dependencies.circe.generic,
      dependencies.circe.parser,
      dependencies.pureconfig.config,
      dependencies.test.core
    )
  )

lazy val proxy = defineProject("proxy", "grpc-prime-numbers-proxy", "services/proxy")
  .aggregate(logging)
  .dependsOn(logging)

lazy val generator = defineProject("generator", "grpc-prime-numbers-generator", "services/generator")
  .aggregate(logging)
  .dependsOn(logging)

lazy val root = (project in file("."))
  .aggregate(proxy, generator)
