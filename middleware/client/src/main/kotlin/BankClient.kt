import generated.bank.*
import org.apache.thrift.protocol.TBinaryProtocol
import org.apache.thrift.protocol.TMultiplexedProtocol
import org.apache.thrift.transport.TSocket
import java.io.BufferedReader

fun main() {

    val creatorTransport = TSocket("localhost", 8080)
    val accountTransport = TSocket("localhost", 8000)

    val accountProtocol = TBinaryProtocol(accountTransport)

    val accountCreatorClient: AccountCreator.Client = AccountCreator.Client(TBinaryProtocol(creatorTransport))
    val accountClient: Account.Client = Account.Client(
            TMultiplexedProtocol(accountProtocol, "AccountService"))
    val premiumAccountClient: PremiumAccount.Client = PremiumAccount.Client(
            TMultiplexedProtocol(accountProtocol, "PremiumAccountService"))

    creatorTransport.open()
    accountTransport.open()


    val input: BufferedReader = java.io.BufferedReader(java.io.InputStreamReader(System.`in`))
    loop@ while (true) {

        when (readLine(input)) {
            "create" -> {
                println("Insert first name")
                val firstName = readLine(input) ?: break@loop

                println("Insert last name")
                val lastName = readLine(input) ?: break@loop

                println("Insert PESEL number")
                val pesel = readLine(input) ?: break@loop

                println("Insert income")
                val income = readLine(input) ?: break@loop

                createAccount(
                        accountCreatorClient,
                        UserData(firstName, lastName, pesel, income.toLong())
                )
            }
            "balance" -> {
                println("Enter pesel")
                val pesel = readLine(input) ?: break@loop

                println("Enter password")
                val password = readLine(input) ?: break@loop

                try {
                    val balance = getBalance(
                            accountClient,
                            AccountCredential(pesel, password)
                    )
                    println("Your balance is $balance")

                } catch (e: AuthorizationFailed) {
                    println("Authorization failed")
                }
            }
            "loan" -> {
                println("Enter pesel")
                val pesel = readLine(input) ?: break@loop

                println("Enter password")
                val password = readLine(input) ?: break@loop

                println("Enter currency")
                val currency = readLine(input) ?: break@loop

                println("Enter amount")
                val amount = readLine(input) ?: break@loop

                println("Enter duration")
                val duration = readLine(input) ?: break@loop

                try {
                    val loanResult = premiumAccountClient.applyForLoan(
                            AccountCredential(pesel, password),
                            LoanRequestInfo(
                                    Currency.valueOf(currency.toUpperCase()),
                                    amount.toLong(),
                                    duration.toLong()
                            )
                    )

                    println(loanResult)

                } catch (e: AuthorizationFailed) {
                    println("Authorization failed")
                } catch (e: CurrencyNotSupported) {
                    println("Currency not supported")
                }


            }
            else -> break@loop
        }
    }


}

fun readLine(input: BufferedReader): String? {
    println("==> ")
    System.out.flush()
    return input.readLine()
}

fun createAccount(client: AccountCreator.Client, userData: UserData) {
    try {
        val result: AccountCreationResult = client.createAccount(userData)
        print(result)

    } catch (ex: IdAlreadyInUse) {
        println("PESEL already in use")
    }
}


@Throws(AuthorizationFailed::class)
fun getBalance(client: Account.Client, credential: AccountCredential): Long {
    return client.getAccountBalance(credential)
}

