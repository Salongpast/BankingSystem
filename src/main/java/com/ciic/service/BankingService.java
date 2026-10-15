package com.ciic.service;

import com.ciic.DaoImpl.AccountDaoImpl;
import com.ciic.model.Account;

import java.math.BigDecimal;
import java.sql.SQLException;

public class BankingService {
    private final AccountDaoImpl accountDAO = new AccountDaoImpl();

    public Account createAccount(String number, String name, BigDecimal initialDeposit) throws SQLException {
        if (number == null || number.isBlank()) {
            throw new IllegalArgumentException("Account number must not be empty.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Account holder name must not be empty.");
        }
        if (initialDeposit == null || initialDeposit.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(initialDeposit + " must be zero or greater.");
        }
        if (accountDAO.exists(number)) {
            throw new IllegalArgumentException("That account number already exists.");
        }

        Account account = accountDAO.create(number, name.trim(), initialDeposit);


        return null;
    }
}
