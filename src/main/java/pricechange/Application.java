package pricechange;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

import javax.mail.Authenticator;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Properties;

@SpringBootApplication
@PropertySource("classpath:mail.properties")
public class Application {

    @Value("${username}")
    static String username;

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);

        Properties props = new Properties();
        props.put("mail.pop3.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.pop3.socketFactory.fallback", "false");
        props.put("mail.pop3.socketFactory.port", "995");
        props.put("mail.pop3.port", "995");
        props.put("mail.pop3.host", "pop.mail.ru");
        props.put("mail.pop3.user", "ali.da@mail.ru");
        props.put("mail.store.protocol", "pop3");

        Session session = Session.getDefaultInstance(props, new MailAuthenticator());
        Store store = session.getStore("pop3");
        store.connect("pop.mail.ru", null, null);
        Folder[] list = store.getDefaultFolder().list("*");

        for (Folder f : list) {
            System.out.println(f);
        }

        store.close();
    }

    static class MailAuthenticator extends Authenticator {
        public PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication("", "");
        }
    }
}
