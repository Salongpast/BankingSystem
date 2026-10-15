package com.ciic.dao;

import com.ciic.model.Transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface TransactionDao {
    void create(Connection connection, Transaction transaction) throws SQLException;

    List<Transaction> findByAccountNumber(Connection connection, String accountNumber) throws SQLException;
}
