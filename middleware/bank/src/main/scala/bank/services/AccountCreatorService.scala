package bank.services

import bank.Utilities.PasswordManager
import bank._
import com.twitter.util.Future

import scala.util.{Failure, Success}

class AccountCreatorService(private val bankPolicy: BankIncomePolicy)(implicit private val database: BankDatabase)
  extends AccountCreator.MethodPerEndpoint {

  override def createAccount(userData: UserData): Future[AccountCreationResult] = {
    println("creating account...")

    val password = PasswordManager.generatePassword()

    val accountCreationResult = AccountCreationResult(
      bankPolicy.getAccountTypeByIncome(userData.monthlyIncome),
      password
    )

    database.addWithPassword(userData, password) match {
      case Success(_) => Future.value(accountCreationResult)
      case Failure(exception) => Future.exception(exception)
    }
  }
}
