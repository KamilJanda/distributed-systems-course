package services

import bank._
import com.twitter.util.Future


class PremiumAccountService(implicit private val database: BankDatabase) extends PremiumAccount.MethodPerEndpoint {

  override def applyForLoan(credential: AccountCredential, loanRequest: LoanRequestInfo): Future[LoanResultInfo] = ???

  override def getAccountBalance(credential: AccountCredential): Future[Long] =
    super.getAccountBalance(credential)
}
