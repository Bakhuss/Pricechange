package pricechange.mail.model;

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
}
