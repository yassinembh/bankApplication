class CheckingAccount extends Account {
    public CheckingAccount(String name, double initialBalance, int accountId) {
        super(name, initialBalance, accountId, "checking");
    }

    @Override
    public void withdraw(double amount) {
        if (amount > 0 && amount <= balance) {
            balance -= amount;
            System.out.println(amount + " euros have been withdrawn from your account.");
        } else {
            System.out.println("Insufficient balance or invalid amount.");
        }
    }

    @Override
    public void displayBalance() {
        System.out.println("Your balance is " + balance + " euros.");
    }
}