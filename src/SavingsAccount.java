class SavingsAccount extends Account {
    private static final double INTEREST_RATE = 0.05;
    private static final double MAX_WITHDRAWAL = 1000;

    public SavingsAccount(String name, double initialBalance, int accountId) {
        super(name, initialBalance, accountId, "savings");
    }

    @Override
    public void withdraw(double amount) {
        if (amount > 0 && amount <= balance && amount <= MAX_WITHDRAWAL) {
            balance -= amount;
            System.out.println(amount + " euros have been withdrawn from your account.");
        } else {
            System.out.println("Invalid amount or exceeds the maximum allowed withdrawal.");
        }
    }

    public void calculateInterest() {
        double interest = balance * INTEREST_RATE;
        balance += interest;
        System.out.println("Interest for this month is " + interest + " euros.");
    }

    @Override
    public void displayBalance() {
        System.out.println("Your balance is " + balance + " euros.");
    }
}