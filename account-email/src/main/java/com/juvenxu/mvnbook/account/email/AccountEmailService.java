package com.juvenxu.mvnbook.account.email;

import javax.mail.MessagingException;

public interface AccountEmailService {
    void sendMail(String to, String subject, String htmlText) throws AccountEmailException, MessagingException;
}