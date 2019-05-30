package com.library.server.actors

import akka.actor.{Actor, Props}
import com.library.messages.SearchBookResponse

import scala.io.Source

class DatabaseSearchWorker extends Actor {

  import DatabaseSearchWorker._

  override def receive: Receive = {
    case Search(title, filename) =>
      for (line <- Source.fromFile(filename).getLines) {
        if (line.contains(title)) {
          val bookAsString = line.split(",").toSeq
          bookAsString.foreach(println)
          sender ! SearchBookResponse(Some(bookAsString(1).toInt))
        }
      }
      sender ! NotFound
      context.stop(self)
  }
}

object DatabaseSearchWorker {

  case class Search(title: String, filename: String)

  case object NotFound

  def props: Props = Props[DatabaseSearchWorker]
}
