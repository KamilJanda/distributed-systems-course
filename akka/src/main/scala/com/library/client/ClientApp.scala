package com.library.client

import java.io.{BufferedReader, File, InputStreamReader}

import scala.concurrent.duration._
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import com.library.messages._
import com.typesafe.config.ConfigFactory

import scala.concurrent.Await

object ClientApp extends App {

  implicit val timeout: Timeout = Timeout(5 seconds)

  val configFile = getClass.getClassLoader.getResource("client_app.conf").getFile
  val config = ConfigFactory.parseFile(new File(configFile))

  val remotePath = "akka.tcp://libraryServer@127.0.0.1:2552/user"

  val system = ActorSystem("libraryClient", config)

  val br = new BufferedReader(new InputStreamReader(System.in))
  while (true) {
    br.readLine match {
      case "find" =>
        println("insert title: ")
        val title = br.readLine()
        val server = system.actorSelection(remotePath + "/librarySystemSupervisor/searchBookService")

        val response = server ? SearchBookRequest(title)
        val result = Await.result(response, timeout.duration).asInstanceOf[SearchBookResponse]

        println("Book price: " + result.price.getOrElse("No record in database"))

      case "order" =>
        println("insert title: ")
        val title = br.readLine()
        val server = system.actorSelection(remotePath + "/librarySystemSupervisor/orderBookService")

        val response = server ? OrderBookRequest(title)
        val result = Await.result(response, timeout.duration).asInstanceOf[OrderBookResponse]

        result.result match {
          case OrderSuccess => println("order success")
          case OrderFail => println("order failed")
        }
      case "stream" =>
        println("insert title: ")
        val title = br.readLine()
        val server = system.actorSelection(remotePath + "")

      case _ =>
        println("unknown command")
    }
  }

  sys.addShutdownHook(system.terminate())

}
