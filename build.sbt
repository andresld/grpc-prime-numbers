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

def defineProject(projectName: String, projectDirectory: String): Project =
  Project(projectName, file(projectDirectory))
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

lazy val proxy = defineProject("grpc-prime-numbers-proxy", "services/proxy")

lazy val generator = defineProject("grpc-prime-numbers-generator", "services/generator")

lazy val root = (project in file("."))
  .aggregate(proxy, generator)
