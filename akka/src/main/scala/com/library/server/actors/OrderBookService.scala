package com.library.server.actors

import java.io.{File, FileNotFoundException, FileWriter, IOException}

import akka.actor.{Actor, ActorLogging, Props}
import com.library.messages._

class OrderBookService(orderDbFile: File) extends Actor with ActorLogging {

  val fileWriter = new FileWriter(orderDbFile, true)

  override def receive: Receive = {
    case OrderBookRequest(title) =>
      log.info("Saving the order to the database...")

      var response = OrderBookResponse(OrderFail)

      try {
        fileWriter.write(s"{title: $title\n")
        fileWriter.flush()
        response = OrderBookResponse(OrderSuccess)
      } catch {
        case _: FileNotFoundException => log.info(s"Couldn't find $title title.")
        case _: IOException => log.info("Got an IOException!")
      }

      sender() ! response
  }
}

object OrderBookService {
  def props(orderDbFile: File): Props = Props(new OrderBookService(orderDbFile))
}
