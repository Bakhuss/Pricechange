package pricechange;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import pricechange.mail.service.MailService;

@SpringBootApplication
public class Application {
    private static MailService mailService;

    @Autowired
    public Application(MailService mailService) {
        Application.mailService = mailService;
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
        System.out.println("list: " + mailService.receiveMessage().size());
//        mailService.sendMessage("message");
    }
}
