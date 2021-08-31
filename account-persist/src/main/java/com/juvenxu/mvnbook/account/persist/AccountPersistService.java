package com.juvenxu.mvnbook.account.persist;

public interface AccountPersistService {
    void createAccount(Account account) throws AccountPersistException;

    Account readAccount(String id) throws AccountPersistException;

    Account updateAccount(Account account) throws AccountPersistException;

    Account deleteAccount(String id) throws AccountPersistException;
}
