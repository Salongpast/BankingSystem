
package com.ciic.DaoImpl;

import com.ciic.dao.TransactionDao;
import com.ciic.database.DatabaseConnection;
import com.ciic.model.Transaction;
import com.ciic.model.TransactionType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class TransactionDaoImpl implements TransactionDao {

    private static final String INSERT_TRANSACTION =
            "INSERT INTO transactions " +
                    "(transaction_reference, account_number, transaction_type, " +
                    "amount, balance, reference_number, remarks) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String FIND_BY_ACCOUNT_NUMBER =
            "SELECT id, transaction_reference, account_number, " +
                    "transaction_type, amount, balance, reference_number, " +
                    "created_at, remarks " +
                    "FROM transactions " +
                    "WHERE account_number = ? " +
                    "ORDER BY created_at DESC, id DESC";

    private static final String FIND_RECENT_TRANSACTIONS =
            "SELECT id, transaction_reference, account_number, " +
                    "transaction_type, amount, balance, reference_number, " +
                    "created_at, remarks " +
                    "FROM transactions " +
                    "WHERE account_number = ? " +
                    "ORDER BY created_at DESC, id DESC " +
                    "LIMIT ?";

    @Override
    public List<Transaction> findByAccountNumber(String accountNumber)
            throws SQLException {

        List<Transaction> transactions = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     conn.prepareStatement(FIND_BY_ACCOUNT_NUMBER)) {

            statement.setString(1, accountNumber);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    transactions.add(mapRow(result));
                }
            }
        }

        return transactions;
    }

    @Override
    public List<Transaction> findRecentTransactions(
            String accountNumber, int limit) throws SQLException {

        if (limit <= 0) {
            throw new IllegalArgumentException(
                    "Limit must be greater than zero.");
        }

        List<Transaction> transactions = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement statement =
                     conn.prepareStatement(FIND_RECENT_TRANSACTIONS)) {

            statement.setString(1, accountNumber);
            statement.setInt(2, limit);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    transactions.add(mapRow(result));
                }
            }
        }

        return transactions;
    }

    @Override
    public void save(Transaction transaction) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            save(conn, transaction);
        }
    }

    @Override
    public void save(Connection conn, Transaction transaction)
            throws SQLException {

        try (PreparedStatement statement =
                     conn.prepareStatement(
                             INSERT_TRANSACTION,
                             Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, transaction.getReferenceNumber());
            statement.setString(2, transaction.getAccountNumber());
            statement.setString(3, transaction.getTransactionType().name());
            statement.setBigDecimal(4, transaction.getAmount());
            statement.setBigDecimal(5, transaction.getBalanceAfter());

            // The current Transaction model has only one reference field.
            statement.setString(6, transaction.getReferenceNumber());

            statement.setString(7, transaction.getRemarks());

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    transaction.setTransactionId(keys.getLong(1));
                }
            }
        }
    }

    private Transaction mapRow(ResultSet result) throws SQLException {
        Transaction transaction = new Transaction();

        transaction.setTransactionId(result.getLong("id"));
        transaction.setAccountNumber(result.getString("account_number"));

        transaction.setTransactionType(
                TransactionType.valueOf(
                        result.getString("transaction_type")));

        transaction.setAmount(result.getBigDecimal("amount"));
        transaction.setBalanceAfter(result.getBigDecimal("balance"));
        transaction.setReferenceNumber(result.getString("reference_number"));
        transaction.setRemarks(result.getString("remarks"));

        Timestamp createdAt = result.getTimestamp("created_at");

        if (createdAt != null) {
            transaction.setCreatedAt(createdAt.toLocalDateTime());
        }

        return transaction;
    }
}
