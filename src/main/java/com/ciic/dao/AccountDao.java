package com.ciic.dao;

import com.ciic.model.Account;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface AccountDao {
    boolean exists(String accountNumber);
    Account create(String accountNumber, String accountName, BigDecimal initialDeposit);
    Account findByAccountNumber(String accountNumber);
    List<Account> findAll() throws SQLException;
    Account mapAccount(ResultSet result) throws SQLException;
}
