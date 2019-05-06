package bank.services

import bank.Utilities.CurrencyTypeConverter
import bank._
import com.twitter.util.Future
import currencyService.CurrencyExchangeClient

import scala.util.{Failure, Success}


class PremiumAccountService(currencyExchangeClient: CurrencyExchangeClient)
                           (implicit private val database: BankDatabase)
  extends PremiumAccount.MethodPerEndpoint {

  override def applyForLoan(credential: AccountCredential, loanRequest: LoanRequestInfo): Future[LoanResultInfo] = {
    val AccountCredential(pesel, password) = credential

    database.authenticate(pesel, password) match {
      case Success(_) =>
        if (!currencyExchangeClient.currencies.contains(CurrencyTypeConverter.convert(loanRequest.currency)))
          Future.exception(new CurrencyNotSupported("Currency not supported"))
        else {
          val rates = currencyExchangeClient.currencyRates

          val loanCost = loanRequest.loanAmount * 2
          val loanForeignCost = loanCost * rates(CurrencyTypeConverter.convert(loanRequest.currency))

          Future.value(LoanResultInfo(accepted = true, Some(loanCost), Some(loanForeignCost.toLong)))
        }
      case Failure(exception) => Future.exception(exception)
    }
  }

  override def getAccountBalance(credential: AccountCredential): Future[Long] = {
    val AccountCredential(pesel, password) = credential

    database.authenticate(pesel, password) match {
      case Success(_) =>
        Future.value(database.getUser(pesel).get.monthlyIncome)
      case Failure(exception) => Future.exception(exception)
    }
  }
}
