import sbt.Keys._
import sbt.{Resolver, _}

object Dependencies {

  val scrooge = Seq(
    "org.apache.thrift" % "libthrift" % "0.12.0",
    "com.twitter" %% "scrooge-core" % "19.4.0" exclude("com.twitter", "libthrift"),
    "com.twitter" %% "finagle-thrift" % "19.4.0" exclude("com.twitter", "libthrift"),
  )

  val all = scrooge
}