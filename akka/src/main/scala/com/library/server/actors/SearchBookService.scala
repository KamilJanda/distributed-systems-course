package com.library.server.actors

import akka.actor.SupervisorStrategy.Restart
import akka.actor.{Actor, ActorLogging, OneForOneStrategy, Props, SupervisorStrategy}
import com.library.messages._
import scala.concurrent.duration._

class SearchBookService extends Actor with ActorLogging {

  override def preStart() = {
    log.info("Search service start")
  }

  override def receive: Receive = {
    case searchBookRequest: SearchBookRequest =>
      val worker = context.actorOf(SearchBookWorker.props())
      worker.forward(searchBookRequest)
    case _ =>
      log.info("unknown message")

  }

  override def supervisorStrategy: SupervisorStrategy = {
    OneForOneStrategy(maxNrOfRetries = 3, withinTimeRange = 1 minute) {
      case _: Exception => Restart
    }
  }
}

object SearchBookService {
  def props(): Props = Props(new SearchBookService)
}
