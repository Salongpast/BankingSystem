package com.ciic.service;

import com.ciic.DaoImpl.AccountDaoImpl;
import com.ciic.DaoImpl.TransactionDaoImpl;
import com.ciic.dao.AccountDao;
import com.ciic.dao.TransactionDao;
import com.ciic.database.DatabaseConnection;
import com.ciic.model.Account;
import com.ciic.model.Transaction;
import com.ciic.model.TransactionType;
import com.ciic.util.ReferenceNumberGenerator;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class TransactionService {
    private final AccountDao accountDao;
    private final TransactionDao transactionDao;

    public TransactionService() {
        this(new AccountDaoImpl(), new TransactionDaoImpl());
    }

    public TransactionService(AccountDao accountDao, TransactionDao transactionDao) {
        this.accountDao = accountDao;
        this.transactionDao = transactionDao;
    }

    public Transaction deposit(String accountNumber, BigDecimal amount)
            throws SQLException {
        validateAmount(amount);
        Account account = requireAccount(accountNumber);
        BigDecimal newBalance = account.getBalance().add(amount);
        return recordBalanceChange(account, newBalance, amount,
                TransactionType.DEPOSIT, "Cash deposit");
    }

    public Transaction withdraw(String accountNumber, BigDecimal amount)
            throws SQLException {
        validateAmount(amount);
        Account account = requireAccount(accountNumber);
        if (account.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance.");
        }
        BigDecimal newBalance = account.getBalance().subtract(amount);
        return recordBalanceChange(account, newBalance, amount,
                TransactionType.WITHDRAW, "Cash withdrawal");
    }

    public void transfer(String fromAccountNumber, String toAccountNumber,
                         BigDecimal amount) throws SQLException {
        validateAmount(amount);
        if (fromAccountNumber == null || toAccountNumber == null
                || fromAccountNumber.trim().isEmpty()
                || toAccountNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Both account numbers are required.");
        }
        if (fromAccountNumber.trim().equals(toAccountNumber.trim())) {
            throw new IllegalArgumentException("Source and destination accounts must be different.");
        }

        Account from = requireAccount(fromAccountNumber);
        Account to = requireAccount(toAccountNumber);
        if (from.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance.");
        }

        BigDecimal fromBalance = from.getBalance().subtract(amount);
        BigDecimal toBalance = to.getBalance().add(amount);
        // transaction_reference is UNIQUE, so each side of a transfer needs its own number.
        Transaction outgoing = new Transaction(from.getAccountNumber(),
                TransactionType.TRANSFER_OUT, amount, fromBalance, ReferenceNumberGenerator.generate(),
                "Transfer to " + to.getAccountNumber());
        Transaction incoming = new Transaction(to.getAccountNumber(),
                TransactionType.TRANSFER_IN, amount, toBalance, ReferenceNumberGenerator.generate(),
                "Transfer from " + from.getAccountNumber());

        try (Connection conn = DatabaseConnection.getConnection()) {
            boolean oldAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                accountDao.updateBalance(conn, from.getAccountNumber(), fromBalance);
                accountDao.updateBalance(conn, to.getAccountNumber(), toBalance);
                transactionDao.save(conn, outgoing);
                transactionDao.save(conn, incoming);
                conn.commit();
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(oldAutoCommit);
            }
        }
    }

    public List<Transaction> getTransactionHistory(String accountNumber)
            throws SQLException {
        requireAccount(accountNumber);
        return transactionDao.findByAccountNumber(accountNumber.trim());
    }

    public List<Transaction> getRecentTransactions(String accountNumber, int limit)
            throws SQLException {
        requireAccount(accountNumber);
        return transactionDao.findRecentTransactions(accountNumber.trim(), limit);
    }

    private Transaction recordBalanceChange(Account account, BigDecimal newBalance,
                                            BigDecimal amount, TransactionType type,
                                            String remarks) throws SQLException {
        String reference = ReferenceNumberGenerator.generate();
        Transaction transaction = new Transaction(account.getAccountNumber(),
                type, amount, newBalance, reference, remarks);

        try (Connection conn = DatabaseConnection.getConnection()) {
            boolean oldAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                accountDao.updateBalance(conn, account.getAccountNumber(), newBalance);
                transactionDao.save(conn, transaction);
                conn.commit();
            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(oldAutoCommit);
            }
        }
        return transaction;
    }

    private Account requireAccount(String accountNumber) throws SQLException {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Account number is required.");
        }
        Optional<Account> account = accountDao.findByAccountNumber(accountNumber.trim());
        if (!account.isPresent()) {
            throw new IllegalArgumentException("Account not found: " + accountNumber);
        }
        if (account.get().getBalance() == null) {
            throw new IllegalStateException("Account balance is missing.");
        }
        return account.get();
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }
    }
}