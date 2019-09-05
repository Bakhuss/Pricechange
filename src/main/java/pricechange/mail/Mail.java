package pricechange.mail;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Properties;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Mail {
    String username;
    String password;
    Properties properties;

    public Mail(Properties properties) {
        this.username = properties.getProperty("mail.username");
        this.password = properties.getProperty("mail.password");
        properties.remove("mail.username");
        properties.remove("mail.password");
        this.properties = properties;
    }
}
