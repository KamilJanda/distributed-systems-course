package bank

import scala.collection.mutable
import scala.util.{Failure, Success, Try}

class BankDatabase extends Authenticator {
  val database: mutable.Map[String, UserData] = mutable.Map()

  def add(userData: UserData): Try[UserData] = {

    if (database.contains(userData.pesel))
      Failure(new IdAlreadyInUse("User already in base"))
    else {
      database.put(userData.pesel, userData)
      Success(userData)
    }

  }

  def getUser(peselId: String): Option[UserData] =
    database.get(peselId)

}
