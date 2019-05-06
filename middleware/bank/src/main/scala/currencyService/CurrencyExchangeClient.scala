package currencyService

import exchange.{Currency, CurrencyRate, ExchangeRateRequest, ExchangeRateServiceGrpc}
import io.grpc.ManagedChannelBuilder

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


class CurrencyExchangeClient(
                              val baseCurrency: Currency,
                              val currencies: Seq[Currency]
                            ) {

  val currencyRates: mutable.Map[Currency, Double] = mutable.Map()


  def start() = {
    import CurrencyExchangeClient._

    val channel = ManagedChannelBuilder.forAddress("localhost", port).usePlaintext().build()
    val blockingStub = ExchangeRateServiceGrpc.blockingStub(channel)

    blockingStub.getInitialExchangeRates(ExchangeRateRequest(baseCurrency, currencies))
      .exchangeRateInfo
      .foreach { case CurrencyRate(currency, rate) => println(s"Currency: $currency: $rate") }

    Future {
      blockingStub.getExchangeRateStream(ExchangeRateRequest(baseCurrency, currencies))
        .foreach(response => response.exchangeRateInfo.foreach(currencyRate =>
          currencyRates.synchronized {
            currencyRates.put(currencyRate.currency, currencyRate.rate)
          }
        ))
    }
  }


}

object CurrencyExchangeClient {
  val port = 30300
}
