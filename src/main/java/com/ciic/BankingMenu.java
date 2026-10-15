package com.ciic;

import com.ciic.model.Account;
import com.ciic.model.Transaction;
import com.ciic.model.TransactionType;
import com.ciic.service.AccountService;
import com.ciic.service.TransactionService;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

/** Console menu for the CIIC banking application. */
public class BankingMenu {

    private static final String LINE = "  " + "─".repeat(52);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final String MENU = """

               BANKING SYSTEM
            %s
              1  Create account     5  Withdraw
              2  View account       6  Transfer
              3  List accounts      7  History
              4  Deposit            0  Exit
            %s
            """.formatted(LINE, LINE);

    private final Scanner in = new Scanner(System.in);
    private String operation = "";
    private final AccountService accounts = new AccountService();
    private final TransactionService transactions = new TransactionService();

    public void run() {
        while (true) {
            System.out.print(MENU);
            String choice = ask("Select");
            try {
                switch (choice) {
                    case "1" -> createAccount();
                    case "2" -> viewAccount();
                    case "3" -> listAccounts();
                    case "4" -> deposit();
                    case "5" -> withdraw();
                    case "6" -> transfer();
                    case "7" -> history();
                    case "0" -> {
                        System.out.println("  Goodbye.");
                        return;
                    }
                    default -> System.out.println("\n  Invalid option. Choose 0-7.");
                }
            } catch (SQLException e) {
                System.out.println("\n  ✗ " + operation + " failed (database error): " + e.getMessage());
            } catch (RuntimeException e) {
                System.out.println("\n  ✗ " + operation + " failed: " + e.getMessage());
            }
            ask("Press Enter");
        }
    }

    // ---- Actions ----------------------------------------------------------------

    private void createAccount() throws SQLException {
        start("Create account", "Opening a new account with an initial deposit.");
        Account a = accounts.createAccount(ask("Account no."), ask("Holder"), amount("Initial deposit", true));
        done("Account " + a.getAccountNumber() + " opened for " + a.getAccountName()
                + " with a balance of " + money(a.getBalance()) + ".");
    }

    private void viewAccount() throws SQLException {
        start("View account", "Looking up an account by its number.");
        String number = ask("Account no.");
        accounts.findAccount(number).ifPresentOrElse(
                a -> {
                    done("Account " + number + " found.");
                    show("Account", a.getAccountNumber(), "Holder", a.getAccountName(),
                            "Balance", money(a.getBalance()), "Opened", a.getCreatedAt().format(DATE));
                },
                () -> System.out.println("\n  ✗ No account found with number " + number + "."));
    }

    private void listAccounts() throws SQLException {
        start("List accounts", "Showing every account on record.");
        List<Account> list = accounts.getAllAccounts();
        done("Found " + list.size() + " account(s).");
        System.out.println(LINE);
        System.out.printf("  %-14s %-24.24s %12s%n", "Account", "Holder", "Balance");
        System.out.println(LINE);
        list.forEach(a -> System.out.printf("  %-14s %-24.24s %12s%n",
                a.getAccountNumber(), a.getAccountName(), money(a.getBalance())));
        System.out.println(LINE);
    }

    private void deposit() throws SQLException {
        start("Deposit", "Adding money to an account.");
        Transaction t = transactions.deposit(ask("Account no."), amount("Amount", false));
        done("Deposited " + money(t.getAmount()) + " into account " + t.getAccountNumber()
                + ". New balance is " + money(t.getBalanceAfter()) + ".");
        System.out.println("  Reference: " + t.getReferenceNumber());
    }

    private void withdraw() throws SQLException {
        start("Withdraw", "Taking money out of an account.");
        Transaction t = transactions.withdraw(ask("Account no."), amount("Amount", false));
        done("Withdrew " + money(t.getAmount()) + " from account " + t.getAccountNumber()
                + ". New balance is " + money(t.getBalanceAfter()) + ".");
        System.out.println("  Reference: " + t.getReferenceNumber());
    }

    private void transfer() throws SQLException {
        start("Transfer", "Moving money from one account to another.");
        String from = ask("From account");
        String to = ask("To account");
        BigDecimal amt = amount("Amount", false);
        transactions.transfer(from, to, amt);
        done("Transferred " + money(amt) + " from account " + from + " to account " + to
                + ". Both balances were updated.");
    }

    private void history() throws SQLException {
        start("History", "Listing all transactions for an account.");
        String number = ask("Account no.");
        List<Transaction> list = transactions.getTransactionHistory(number);
        done("Found " + list.size() + " transaction(s) for account " + number + ".");
        System.out.println(LINE);
        System.out.printf("  %-16s %-13s %11s %11s%n", "Date", "Type", "Amount", "Balance");
        System.out.println(LINE);
        for (Transaction t : list) {
            boolean credit = t.getTransactionType() == TransactionType.DEPOSIT
                    || t.getTransactionType() == TransactionType.TRANSFER_IN;
            System.out.printf("  %-16s %-13s %11s %11s%n", t.getCreatedAt().format(DATE),
                    t.getTransactionType().displayName(),
                    (credit ? "+" : "-") + money(t.getAmount()), money(t.getBalanceAfter()));
        }
        System.out.println(LINE);
    }

    // ---- Helpers ----------------------------------------------------------------

    /** Announces which operation is now active. */
    private void start(String name, String description) {
        operation = name;
        System.out.println("  ▸ " + name.toUpperCase() + " — " + description + "\n");
    }

    /** States what the active operation has just done. */
    private void done(String message) {
        System.out.println("\n  ✓ " + operation + " complete. " + message);
    }

    /** Prints label/value pairs between two rules. */
    private void show(String... pairs) {
        System.out.println(LINE);
        for (int i = 0; i < pairs.length; i += 2) {
            System.out.printf("  %-12s %s%n", pairs[i], pairs[i + 1]);
        }
        System.out.println(LINE);
    }

    private String ask(String label) {
        System.out.print("  " + label + " › ");
        return in.nextLine().trim();
    }

    /** Re-asks until a valid amount with at most 2 decimals is entered. */
    private BigDecimal amount(String label, boolean allowZero) {
        while (true) {
            try {
                BigDecimal v = new BigDecimal(ask(label).replace(",", ""));
                if (v.scale() <= 2 && v.signum() >= (allowZero ? 0 : 1)) {
                    return v.setScale(2);
                }
            } catch (NumberFormatException ignored) {
            }
            System.out.println("  Enter a valid amount, e.g. 250.00");
        }
    }

    private static String money(BigDecimal v) {
        return v == null ? "0.00" : String.format("%,.2f", v);
    }
}