package pricechange.mail.service;

import javax.mail.MessagingException;

public interface MailService {
    void getNewLetters() throws MessagingException;
}
