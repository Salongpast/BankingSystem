    package com.ciic;

    import com.ciic.model.Account;
    import com.ciic.model.Transaction;
    import com.ciic.service.AccountService;
    import com.ciic.service.TransactionService;

    import java.math.BigDecimal;
    import java.math.RoundingMode;
    import java.sql.SQLException;
    import java.time.format.DateTimeFormatter;
    import java.util.List;
    import java.util.Optional;
    import java.util.Scanner;

    public class BankingMenu {
        private static final String LINE = "─".repeat(56);
        private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        private static final String MENU = """
                %s
                      BANKING SYSTEM
                %s
                    1  Create account
                    2  View account
                    3  List accounts
                    4  Deposit
                    5  Withdraw
                    6  Transfer
                    7  History
                    0  Exit
                %s
                """.formatted("─".repeat(25),"─".repeat(25),"─".repeat(25));

        private final Scanner sc = new Scanner(System.in);
        private final AccountService accountService = new AccountService();
        private final TransactionService transactionService = new TransactionService();

        public void run() {
            while (true) {
                System.out.print(MENU);
                String choice = ask("  > ");
                try {
                    switch (choice) {
                        case "1" -> createAccount();
                        case "2" -> inquireAccount();
                        case "3" -> listAccounts();
                        case "4" -> deposit();
                        case "5" -> withdraw();
                        case "6" -> transfer();
                        case "7" -> history();
                        case "0" -> {
                            System.out.println("  Goodbye.");
                            return;
                        }
                        default -> System.out.println("Invalid operation selection. Please try again.");
                    }
                } catch (RuntimeException | SQLException e) {
                    System.out.println(" failed: " + e.getMessage());
                }
                ask("Press Enter to continue... ");
            }
        }

        private void createAccount() throws SQLException {
            System.out.println("Creating account...");
            String accountNumber = ask("Enter Account Number: ");
            String accountName = ask("Enter Account Name: ");
            BigDecimal initialDeposit = money("Enter Initial Deposit: ");

            Account account = accountService.createAccount(accountNumber,accountName,initialDeposit);

            System.out.println("Account Created.");
            System.out.println(LINE);
            System.out.println("Account Number: " + account.getAccountNumber());
            System.out.println("Account Name: " + account.getAccountName());
            System.out.println("Account Balance: " + formatMoney(account.getBalance()));
            System.out.println(LINE);
        }

        private void inquireAccount() throws SQLException {

            System.out.println("Inquire Account");
            String accountNumber = ask("Enter Account Number: ");
            Optional<Account> accountOptional = accountService.findAccount(accountNumber);

            System.out.println(LINE);
            if (accountOptional.isPresent()) {
                Account account = accountOptional.get();

                System.out.println("Account Number: " + account.getAccountNumber());
                System.out.println("Account Name: " + account.getAccountName());
                System.out.println("Account Balance: " + formatMoney(account.getBalance()));

            } else {
                System.out.println("Error: Account not found.");
            }
            System.out.println(LINE);
        }


        private void listAccounts() throws SQLException {
            String format = "%-15s %-25s %12.2f%n";
            String headerFormat = "%-15s %-25s %12s%n";
            List<Account> accounts = accountService.getAllAccounts();

            System.out.println("List of accounts");
            System.out.println(LINE);
            System.out.printf(headerFormat, "Account Number", "Account Name", "Balance");
            System.out.println(LINE);
            for (Account account : accounts){
                System.out.printf(format,
                        account.getAccountNumber(),
                        account.getAccountName(),
                        account.getBalance()
                );
            }
            System.out.println(LINE);

        }

        private void deposit() throws SQLException {
            System.out.println("Deposit to Account.");
            System.out.println(LINE);
            String accountNumber = ask("Enter Account Number: ");
            BigDecimal amount = money("Enter Deposit Amount: ");
            System.out.println(LINE);
            Transaction t = transactionService.deposit(accountNumber,amount);
            System.out.println("Deposited " + amount + " into account " + t.getAccountNumber()
                    + ". New balance is " + formatMoney(t.getBalanceAfter()) + ".");
            System.out.println(LINE);
        }

        private void withdraw() throws SQLException {
            System.out.println("Taking money out of an account.");
            System.out.println(LINE);
            String accountNumber = ask("Enter Account Number: ");
            BigDecimal amount = money("Enter Withdraw Amount: ");
            System.out.println(LINE);
            Transaction t = transactionService.withdraw(accountNumber,amount);
            System.out.println("Withdrew  " + amount + " into account " + t.getAccountNumber()
                    + ". New balance is " + formatMoney(t.getBalanceAfter()) + ".");
            System.out.println(LINE);
        }

        private void transfer() throws SQLException {
            System.out.println("Transfer between accounts.");
            System.out.println(LINE);

            String fromAccountNumber = ask("Enter Source Account Number: ");
            String toAccountNumber = ask("Enter Destination Account Number: ");
            BigDecimal amount = money("Enter Transfer Amount: ");

            System.out.println(LINE);
            transactionService.transfer(fromAccountNumber, toAccountNumber, amount);

            System.out.println("Successfully transferred " + formatMoney(amount)
                    + " from account " + fromAccountNumber
                    + " to account " + toAccountNumber + ".");
            System.out.println(LINE);
        }

        private void history() throws SQLException {
            System.out.println("Transaction History");
            System.out.println(LINE + "─".repeat(12));

            String accountNumber = ask("Enter Account Number: ");
            List<Transaction> transactions =
                    transactionService.getTransactionHistory(accountNumber);

            if (transactions.isEmpty()) {
                System.out.println("No transactions found for account " + accountNumber + ".");
                System.out.println(LINE);
                return;
            }

            String headerFormat = "%-18s %-15s %8s %8s %-18s%n";
            String rowFormat =    "%-18s %-15s %8s %8s %-18s%n";

            System.out.printf(headerFormat, "Date", "Type", "Amount", "Balance", "Related Account");
            System.out.println(LINE + "─".repeat(12));

            for (Transaction transaction : transactions) {
                String relatedAccount = transaction.getRelatedAccountNumber();

                if (relatedAccount == null || relatedAccount.isBlank()) {
                    relatedAccount = "-";
                }

                String createdAt = transaction.getCreatedAt() == null
                        ? "-"
                        : DATE.format(transaction.getCreatedAt());

                System.out.printf(rowFormat,
                        createdAt,
                        transaction.getTransactionType(),
                        formatMoney(transaction.getAmount()),
                        formatMoney(transaction.getBalanceAfter()),
                        relatedAccount);
            }

            System.out.println(LINE + "─".repeat(12));
        }

        private BigDecimal formatMoney(BigDecimal money){
            return money.setScale(2, RoundingMode.HALF_UP);
        }

        private String ask(String label){
            System.out.print(label);
            return sc.nextLine();
        }

        private BigDecimal money(String label) {
            while (true) {
                System.out.print(label);
                String input = sc.nextLine();

                try {
                    BigDecimal money = new BigDecimal(input);

                    if (money.compareTo(BigDecimal.ZERO) < 0) {
                        System.out.println("Amount cannot be negative.");
                        continue;
                    }

                    return money;
                } catch (NumberFormatException e) {
                    System.out.println("Invalid amount. Please enter a valid number.");
                }
            }
        }

    }