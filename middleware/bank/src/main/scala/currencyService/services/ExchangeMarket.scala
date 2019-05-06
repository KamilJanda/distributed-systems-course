package currencyService.services

import java.util.concurrent.ConcurrentHashMap

import exchange.{Currency, CurrencyRate}

import scala.collection.convert.decorateAsScala._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object ExchangeMarket {
  val currencyRates = new ConcurrentHashMap[Currency, Double]().asScala

  currencyRates.put(Currency.PLN, 1.0)
  currencyRates.put(Currency.USD, 3.2)
  currencyRates.put(Currency.GBP, 4.98)
  currencyRates.put(Currency.EUR, 4.4)


  def run(): Future[Unit] = Future {
    while (true) {
      Thread.sleep(3000)

      val rand = scala.util.Random

      currencyRates.foreach {
        case (key, value) => currencyRates.put(key, value + (rand.nextDouble() / 10))
      }
    }
  }

  def getAllRates: List[CurrencyRate] = {
    currencyRates.map {
      case (currency, value) => CurrencyRate(currency, value)
    }.toList
  }
}
