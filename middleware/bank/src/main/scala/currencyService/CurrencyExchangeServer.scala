package currencyService

import java.util.logging.Logger

import currencyService.services.{ExchangeMarket, ExchangeRateService}
import exchange._
import io.grpc.{Server, ServerBuilder}

import scala.concurrent.ExecutionContext

class CurrencyExchangeServer {
  private var server: Server = _

  private def start() = {
    server = ServerBuilder.forPort(CurrencyExchangeServer.port)
      .addService(ExchangeRateServiceGrpc.bindService(new ExchangeRateService, ExecutionContext.global)).build.start
    CurrencyExchangeServer.logger.info("Server started, listening on " + CurrencyExchangeServer.port)
    sys.addShutdownHook {
      System.err.println("*** shutting down gRPC server since JVM is shutting down")
      stop()
      System.err.println("*** gRPC server shut down")
    }
  }

  private def stop(): Unit = {
    if (server != null) {
      server.shutdown()
    }
  }

  private def blockUntilShutdown(): Unit = {
    if (server != null) {
      server.awaitTermination()
    }
  }
}

object CurrencyExchangeServer extends App {
  private val logger = Logger.getLogger(classOf[CurrencyExchangeServer].getName)
  private val port = 30300

  ExchangeMarket.run()

  val currencyExchangeServer = new CurrencyExchangeServer

  currencyExchangeServer.start()
  currencyExchangeServer.blockUntilShutdown()
}
