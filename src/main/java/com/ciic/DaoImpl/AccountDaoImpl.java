package com.ciic.DaoImpl;

import com.ciic.dao.AccountDao;
import com.ciic.database.DatabaseConnection;
import com.ciic.model.Account;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AccountDaoImpl implements AccountDao {

    @Override
    public boolean exists(String accountNumber) {
        String sql = "SELECT 1 FROM accounts WHERE account_number = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, accountNumber);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking account existence", e);
        }
    }

    @Override
    public Account create(String accountNumber, String accountName, BigDecimal initialDeposit) {
        String sql = "INSERT INTO accounts (account_number, account_name, balance) VALUES (?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, accountNumber);
            statement.setString(2, accountName);
            statement.setBigDecimal(3, initialDeposit);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Database error creating account", e);
        }
        return findByAccountNumber(accountNumber);
    }

    @Override
    public Account findByAccountNumber(String accountNumber) {
        String sql = "SELECT id, account_number, account_name, balance, created_at FROM accounts WHERE account_number = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, accountNumber);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    return mapAccount(result);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching account.",e);
        }
        return null;
    }

    @Override
    public List<Account> findAll() throws SQLException {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT id, account_number, account_name, balance, created_at FROM accounts ORDER BY id";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                accounts.add(mapAccount(result));
            }
        }
        return accounts;
    }

    @Override
    public Account mapAccount(ResultSet result) throws SQLException{
        return new Account(
                result.getLong("id"),
                result.getString("account_number"),
                result.getString("account_name"),
                result.getBigDecimal("balance"),
                result.getTimestamp("created_at").toLocalDateTime()
        );
    }
}
