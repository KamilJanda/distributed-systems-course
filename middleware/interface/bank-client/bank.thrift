namespace * bank

enum Currency {
  PLN = 1,
  USD = 2,
  GBP = 3,
  EUR = 4
}

enum AccountType {
    Standard = 1,
    Premium = 2
}

typedef i64 Money
typedef i64 Months

struct UserData {
    1: string firstName,
    2: string lastName,
    3: string pesel,
    4: Money monthlyIncome
}

struct AccountCredential {
    string pesel,
    string password
}

struct LoanRequestInfo {
    Currency currency,
    Money loanAmount,
    Months duration
}

struct LoanResultInfo {
    required bool accepted,
    optional Money nationalCurrencyCost,
    optional Money foreignCurrencyCost
}

struct AccountCreationResult {
    AccountType accountType,
    string password
}

exception CurrencyNotSupported {
    string message
}

exception IdAlreadyInUse {
    string message
}

exception AuthorizationFailed {
    string message
}

service Account {
    Money getAccountBalance(AccountCredential credential) throws(AuthorizationFailed ex1),

}

service PremiumAccount extends Account {
    LoanResultInfo applyForLoan(AccountCredential credential, LoanRequestInfo loanRequest)
        throws(AuthorizationFailed ex1, CurrencyNotSupported ex2)
}

service AccountCreator {
    AccountCreationResult createAccount(UserData userData) throws(IdAlreadyInUse ex1)
}

