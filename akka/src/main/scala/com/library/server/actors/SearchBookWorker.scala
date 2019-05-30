package com.library.server.actors


import akka.actor.SupervisorStrategy.Restart
import akka.actor.{Actor, ActorLogging, ActorRef, OneForOneStrategy, PoisonPill, Props, SupervisorStrategy}
import com.library.messages._
import com.library.server.actors.DatabaseSearchWorker.{NotFound, Search}
import scala.concurrent.duration._


class SearchBookWorker extends Actor with ActorLogging {

  val db1 = "database/db1.txt"
  val db2 = "database/db2.txt"

  override def receive: Receive = {
    case SearchBookRequest(title) =>
      val crawler1 = context.actorOf(DatabaseSearchWorker.props)
      val crawler2 = context.actorOf(DatabaseSearchWorker.props)
      crawler1 ! Search(title, db1)
      crawler2 ! Search(title, db2)
      context.become(waitDbResponse(1, sender()))
  }

  def waitDbResponse(awaiting: Int, originSender: ActorRef): Receive = {
    case NotFound if awaiting > 0 =>
      context.become(waitDbResponse(awaiting - 1, originSender))

    case NotFound =>
      originSender ! SearchBookResponse(None)
      self ! PoisonPill

    case response: SearchBookResponse =>
      originSender ! response
      self ! PoisonPill
  }

  override def supervisorStrategy: SupervisorStrategy = {
    OneForOneStrategy(maxNrOfRetries = 3, withinTimeRange = 1 minute) {
      case _: Exception => Restart
    }
  }

}

object SearchBookWorker {
  def props(): Props = Props(new SearchBookWorker)
}
