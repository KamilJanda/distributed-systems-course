package services

import bank.{Account, AccountCredential, BankDatabase}
import com.twitter.util.Future

import scala.util.{Failure, Success}

class AccountService(implicit private val database: BankDatabase) extends Account.MethodPerEndpoint {
  override def getAccountBalance(credential: AccountCredential): Future[Long] = {
    val AccountCredential(pesel, password) = credential

    database.authenticate(pesel, password) match {
      case Success(_) =>
        Future.value(database.getUser(pesel).get.monthlyIncome)
      case Failure(exception) => Future.exception(exception)
    }
  }
}
