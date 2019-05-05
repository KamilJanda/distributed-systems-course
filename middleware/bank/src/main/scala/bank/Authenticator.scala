package bank

import scala.collection.mutable
import scala.util.{Failure, Success, Try}

trait Authenticator {
  self: BankDatabase =>

  val passwordBase: mutable.Map[String, String] = mutable.Map()

  def addWithPassword(userData: UserData, password: String): Try[UserData] = {
    add(userData) match {
      case Success(_) =>
        passwordBase.put(userData.pesel, password)
        Success(userData)

      case Failure(reason) => Failure(reason)
    }
  }

  def authenticate(pesel: String, password: String): Try[String] = {

    passwordBase.get(pesel) match {
      case Some(passwordVal) if passwordVal == password => Success("OK")
      case _ => Failure(new AuthorizationFailed("Authorization failed"))
    }

  }
}
