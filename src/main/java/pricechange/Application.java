package pricechange;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import pricechange.file.service.File;
import pricechange.mail.model.FileByBytes;
import pricechange.mail.service.MailService;

import java.io.IOException;
import java.util.List;

@SpringBootApplication
public class Application {
    private static MailService mailService;

    @Autowired
    public Application(MailService mailService) {
        Application.mailService = mailService;
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
        List<FileByBytes> fileByBytes = mailService.receiveMessage();
        System.out.println("list: " + fileByBytes.size());
        for (FileByBytes f : fileByBytes) {
            new Thread(() -> {
                try {
                    new File().save(f);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
//        mailService.sendMessage("message");
    }
}
