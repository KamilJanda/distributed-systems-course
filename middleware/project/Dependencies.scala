import sbt.Keys._
import sbt.{Resolver, _}

object Dependencies {

  val scrooge = Seq(
    "org.apache.thrift" % "libthrift" % "0.12.0",
    "com.twitter" %% "scrooge-core" % "19.4.0" exclude("com.twitter", "libthrift"),
    "com.twitter" %% "finagle-thrift" % "19.4.0" exclude("com.twitter", "libthrift"),
  )

  val gRpc = Seq(
    "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf",
    "io.grpc" % "grpc-netty" % scalapb.compiler.Version.grpcJavaVersion,
    "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion,
  )

  val akka = Seq(
    "com.typesafe.akka" %% "akka-actor" % "2.5.21",
    "com.typesafe.akka" %% "akka-stream-typed" % "2.5.21"
  )

  val all = scrooge ++ gRpc ++ akka
}