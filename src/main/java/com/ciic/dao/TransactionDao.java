package com.ciic.dao;

import com.ciic.model.Account;
import com.ciic.model.Transaction;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface TransactionDao {

    List<Transaction> findByAccountNumber(String accountNumber) throws SQLException;

    List<Transaction> findRecentTransactions(String accountNumber, int limit) throws SQLException;

    void save(Transaction transaction) throws SQLException;

    void save(Connection conn, Transaction transaction) throws SQLException;
}
