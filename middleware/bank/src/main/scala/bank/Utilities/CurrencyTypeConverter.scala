package bank.Utilities

object CurrencyTypeConverter {

  def convert(cur: bank.Currency): exchange.Currency = {
    cur match {
      case bank.Currency.Pln => exchange.Currency.PLN
      case bank.Currency.Usd => exchange.Currency.USD
      case bank.Currency.Gbp => exchange.Currency.GBP
      case bank.Currency.Eur => exchange.Currency.EUR
    }
  }

  def convert(cur: exchange.Currency): bank.Currency = {
    cur match {
      case exchange.Currency.PLN => bank.Currency.Pln
      case exchange.Currency.USD => bank.Currency.Usd
      case exchange.Currency.GBP => bank.Currency.Gbp
      case exchange.Currency.EUR => bank.Currency.Eur
    }
  }


}
