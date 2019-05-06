package bank

import java.net.{InetAddress, InetSocketAddress}

import bank.services.{AccountCreatorService, AccountService, PremiumAccountService}
import com.twitter.finagle.thrift.ThriftService
import com.twitter.finagle.{ListeningServer, Thrift}
import currencyService.CurrencyExchangeClient


object BankServer extends App {
  implicit val bankDatabase: BankDatabase = new BankDatabase

  val bankPort: Int = 8000
  val creatorPort: Int = 8080
  val exchangePort: Int = 30300

  val bankIncomePolicy = new BankIncomePolicy(900)

  val currencyClient = new CurrencyExchangeClient(
    exchange.Currency.PLN,
    Seq(
      exchange.Currency.EUR,
      exchange.Currency.USD,
    ))

  val registeredUserServices: Map[String, ThriftService] = Map(
    "PremiumAccountService" -> new PremiumAccountService(currencyClient),
    "AccountService" -> new AccountService()
  )

  val registeredUserServer = Thrift.server.withBufferedTransport().serveIfaces(
    addressBuilder(bankPort),
    registeredUserServices
  )

  val newUserServer: ListeningServer = Thrift.server.withBufferedTransport().serveIface(
    addressBuilder(creatorPort),
    new AccountCreatorService(bankIncomePolicy)
  )

  currencyClient.start()
  Thread.currentThread.join()

  def addressBuilder(port: Int) = new InetSocketAddress(InetAddress.getLoopbackAddress, port)
}
