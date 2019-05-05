package bank

import java.net.{InetAddress, InetSocketAddress}

import com.twitter.finagle.thrift.ThriftService
import com.twitter.finagle.{ListeningServer, Thrift}
import services._


object BankServer extends App {

  val bankPort: Int = 8000
  val creatorPort: Int = 8080
  val exchangePort: Int = 30300

  val bankIncomePolicy = new BankIncomePolicy(900)

  implicit val bankDatabase: BankDatabase = new BankDatabase

  val registeredUserServices: Map[String, ThriftService] = Map(
    "PremiumAccountService" -> new PremiumAccountService(),
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

  Thread.currentThread.join()

  def addressBuilder(port: Int) = new InetSocketAddress(InetAddress.getLoopbackAddress, port)
}
