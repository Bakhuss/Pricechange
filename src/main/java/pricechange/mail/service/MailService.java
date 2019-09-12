package pricechange.mail.service;

import pricechange.mail.model.FileByBytes;

import javax.mail.MessagingException;
import java.util.List;

public interface MailService {
    List<FileByBytes> receiveMessage() throws MessagingException;

    void sendMessage(String fileName) throws MessagingException;
}
