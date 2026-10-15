package com.ciic.DaoImpl;

import com.ciic.dao.TransactionDao;
import com.ciic.model.Transaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TransactionDaoImpl implements TransactionDao {
    @Override
    public void create(Connection connection, Transaction transaction) throws SQLException {
        String sql = "INSERT INTO transactions (transaction_reference, account_number, transaction_type, amount, balance, reference_account) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, transaction.getTransactionReference());
            statement.setString(2, transaction.getAccountNumber());
            statement.setString(3, transaction.getTransactionType());
            statement.setBigDecimal(4, transaction.getAmount());
            statement.setBigDecimal(5, transaction.getBalance());
            statement.setString(6, transaction.getReferenceAccount());
            statement.executeUpdate();
        }
    }

    @Override
    public List<Transaction> findByAccountNumber(Connection connection, String accountNumber) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT id, transaction_reference, account_number, transaction_type, amount, balance, reference_account, created_at FROM transactions WHERE account_number = ? ORDER BY created_at, id";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, accountNumber);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    transactions.add(new Transaction(
                            result.getLong("id"),
                            result.getString("transaction_reference"),
                            result.getString("account_number"),
                            result.getString("transaction_type"),
                            result.getBigDecimal("amount"),
                            result.getBigDecimal("balance"),
                            result.getString("reference_account"),
                            result.getTimestamp("created_at").toLocalDateTime()
                    ));
                }
            }
        }
        return transactions;
    }
}
