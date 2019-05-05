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

scroogeThriftSourceFolder in Compile :=
  baseDirectory.value / "interface/bank-client"

scroogeThriftOutputFolder in Compile :=
  baseDirectory.value / "bank/src/main/generated"