name := "middleware"

version := "0.1"

scalaVersion := "2.12.8"


def module(name: String): sbt.Project = {
  sbt.Project(
    id = name,
    base = file(name)
  ).settings(Common.projectSettings)
}

lazy val bank = module("bank")

lazy val root = (project in file("."))
  .aggregate(bank)
  .dependsOn(bank)
  .settings(libraryDependencies ++= Dependencies.all)

PB.targets in Compile := Seq(
  scalapb.gen(grpc = true) -> baseDirectory.value / "currency-service/src/main/scala/generated"
)

//PB.protoSources in Compile := Seq(
//  baseDirectory.value / "interface/bank-currency-service/exchange.proto"
//)

scroogeThriftSourceFolder in Compile :=
  baseDirectory.value / "interface/bank-client"

scroogeThriftOutputFolder in Compile :=
  baseDirectory.value / "bank/src/main/scala/generated"