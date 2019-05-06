name := "bank"

version := "0.1"

scalaVersion := "2.12.8"

PB.targets in Compile := Seq(
  scalapb.gen(grpc = true) -> sourceDirectory.value / "main/scala/currencyService/generated"
)

libraryDependencies ++= Seq(

  "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf",
  "io.grpc" % "grpc-netty" % scalapb.compiler.Version.grpcJavaVersion,
  "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion
)