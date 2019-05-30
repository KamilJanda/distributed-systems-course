package com.library.server

import java.io.File

import akka.actor.ActorSystem
import com.library.server.actors.LibrarySystemSupervisor
import com.typesafe.config.{Config, ConfigFactory}

object LibraryServer extends App {

  val configFile = getClass.getClassLoader.getResource("server_app.conf").getFile
  val config = ConfigFactory.parseFile(new File(configFile))

  val system = ActorSystem("libraryServer", config)

  val librarySystemSupervisor = system.actorOf(LibrarySystemSupervisor.props(), "librarySystemSupervisor")

  sys.addShutdownHook(system.terminate())
}
