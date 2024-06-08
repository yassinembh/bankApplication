import java.sql.*;
import java.util.*;

abstract class Account {
    protected String name;
    protected int accountId;
    protected double balance;
    protected String type;

    public Account(String name, double initialBalance, int accountId, String type) {
        this.name = name;
        this.balance = initialBalance;
        this.accountId = accountId;
        this.type = type;
    }

    public int getAccountId() {
        return accountId;
    }

    public double getBalance() {
        return balance;
    }

    public void deposit(double amount) {
        if (amount > 0) {
            balance += amount;
            System.out.println(amount + " euros have been deposited into your account.");
        } else {
            System.out.println("Invalid deposit amount.");
        }
    }

    public abstract void withdraw(double amount);

    public abstract void displayBalance();
}