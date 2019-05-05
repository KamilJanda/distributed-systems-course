package bank

import bank.AccountType.{Premium, Standard}

class BankIncomePolicy(incomeThreshold: Long) {

  def getAccountTypeByIncome(income: Long): AccountType =
    if (income > incomeThreshold) Premium else Standard
}
