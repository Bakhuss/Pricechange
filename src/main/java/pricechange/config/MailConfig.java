package pricechange.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pricechange.mail.model.Mail;

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
        try (InputStream is = new FileInputStream("mail.txt");) {
            props.load(is);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Not found property file " + fileName);
        }
        return new Mail(props);
    }
}
