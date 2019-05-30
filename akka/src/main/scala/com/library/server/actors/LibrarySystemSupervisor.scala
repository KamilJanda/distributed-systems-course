package com.library.server.actors

import java.io.File

import akka.actor.SupervisorStrategy.Restart
import akka.actor.{Actor, OneForOneStrategy, Props, SupervisorStrategy}
import scala.concurrent.duration._

class LibrarySystemSupervisor extends Actor {
  val orderDbPath = "database/orders.txt"

  private val orderBookService = context.actorOf(OrderBookService.props(new File(orderDbPath)), "orderBookService")
  private val searchBookService = context.actorOf(SearchBookService.props(), "searchBookService")

  override def receive: Receive = Actor.emptyBehavior

  override def supervisorStrategy: SupervisorStrategy = {
    OneForOneStrategy(maxNrOfRetries = 3, withinTimeRange = 1 minute) {
      case _: Exception => Restart
    }
  }
}

object LibrarySystemSupervisor {
  def props(): Props = Props(new LibrarySystemSupervisor)
}
