package pricechange.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pricechange.mail.model.Mail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Configuration
public class MailConfig {
    @Bean
    public Mail mail() {
        String fileName = "mail.txt";
        Properties props = new Properties();
        System.out.println(new File(fileName).exists());
        System.out.println(new File(fileName).getAbsolutePath());
        try {
            InputStream is = new FileInputStream("mail.txt");
            props.load(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Not found property file " + fileName);
        }
        System.out.println(props.getProperty("mail.username"));
        System.out.println(props.getProperty("mail.password"));
        props.list(System.out);
        Mail mail = new Mail(props);
        return mail;
    }
}
