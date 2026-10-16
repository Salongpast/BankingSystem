
package com.ciic.DaoImpl;

import com.ciic.dao.AccountDao;
import com.ciic.database.DatabaseConnection;
import com.ciic.model.Account;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AccountDaoImpl implements AccountDao {

    private static final String INSERT_ACCOUNT =
            "INSERT INTO accounts (account_number, account_name, balance) VALUES (?, ?, ?)";

    private static final String FIND_BY_ACCOUNT_NUMBER =
            "SELECT id, account_number, account_name, balance, created_at " +
                    "FROM accounts WHERE account_number = ?";

    private static final String FIND_ALL =
            "SELECT id, account_number, account_name, balance, created_at " +
                    "FROM accounts ORDER BY id";

    private static final String UPDATE_BALANCE =
            "UPDATE accounts SET balance = ? WHERE account_number = ?";

    @Override
    public void createAccount(Account account) throws SQLException{
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(INSERT_ACCOUNT, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, account.getAccountNumber());
            preparedStatement.setString(2, account.getAccountName());
            preparedStatement.setBigDecimal(3, account.getBalance());
            preparedStatement.executeUpdate();

            try (ResultSet keys = preparedStatement.getGeneratedKeys()) {
                if (keys.next()) {
                    account.setId(keys.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create account", e);
        }
    }

    @Override
    public Optional<Account> findByAccountNumber(String accountNumber) {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement =
                     connection.prepareStatement(FIND_BY_ACCOUNT_NUMBER)) {

            preparedStatement.setString(1, accountNumber);

            try (ResultSet result = preparedStatement.executeQuery()) {
                if (result.next()) {
                    return Optional.of(mapRow(result));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching account.", e);
        }
    }

    @Override
    public List<Account> findAllAccounts() throws SQLException {
        List<Account> accounts = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(FIND_ALL);
             ResultSet result = preparedStatement.executeQuery()) {

            while (result.next()) {
                accounts.add(mapRow(result));
            }
            return accounts;
        }catch (SQLException e){
            throw new RuntimeException("Failed to list all accounts", e);
        }
    }

    @Override
    public void updateBalance(String accountNumber, BigDecimal balance) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            updateBalance(connection, accountNumber, balance);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update balance",e);
        }
    }

    @Override
    public void updateBalance(Connection conn, String accountNumber, BigDecimal balance)
            throws SQLException {

        try (PreparedStatement ps = conn.prepareStatement(UPDATE_BALANCE)) {
            ps.setBigDecimal(1, balance);
            ps.setString(2, accountNumber);
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("Balance update affected 0 rows for account: " + accountNumber);
            }
        }
    }

    private Account mapRow(ResultSet result) throws SQLException {
        return new Account(
                result.getLong("id"),
                result.getString("account_number"),
                result.getString("account_name"),
                result.getBigDecimal("balance"),
                result.getTimestamp("created_at").toLocalDateTime()
        );
    }
}
