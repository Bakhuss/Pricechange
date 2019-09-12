package pricechange.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pricechange.mail.model.Mail;

import java.util.Properties;

@Configuration
public class MailConfig {
    private final Properties mailProps;

    @Autowired
    public MailConfig(Properties mailProps) {
        this.mailProps = mailProps;
    }

    @Bean
    public Mail mailIn() {
        Mail mail = new Mail();
        mail.setUsername(mailProps.getProperty("in.username"));
        mail.setPassword(mailProps.getProperty("in.password"));

        Properties inProps = new Properties();
        inProps.setProperty("mail.inbox.folder", mailProps.getProperty("mail.inbox.folder"));
        inProps.setProperty("mail.store.protocol", mailProps.getProperty("mail.store.protocol"));
        inProps.setProperty("mail.host", mailProps.getProperty("mail.host"));
        inProps.setProperty("mail.imap.port", mailProps.getProperty("mail.imap.port"));
        inProps.setProperty("mail.imap.ssl.enable", mailProps.getProperty("mail.imap.ssl.enable"));
        mail.setProperties(inProps);
        return mail;
    }

    @Bean
    public Mail mailOut() {
        Mail mail = new Mail();
        mail.setUsername(mailProps.getProperty("out.username"));
        mail.setPassword(mailProps.getProperty("out.password"));

        Properties outProps = new Properties();
        outProps.setProperty("mail.out.to", mailProps.getProperty("mail.out.to"));
        outProps.setProperty("mail.smtp.host", mailProps.getProperty("mail.smtp.host"));
        outProps.setProperty("mail.smtp.port", mailProps.getProperty("mail.smtp.port"));
//        outProps.setProperty("mail.smtp.auth", mailProps.getProperty("mail.smtp.auth"));
//        outProps.setProperty("mail.smtp.ssl.enable", mailProps.getProperty("mail.smtp.ssl.enable"));
//        outProps.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        mail.setProperties(outProps);
        return mail;
    }

//    @Bean
//    public MailReceiver mailReceiver() {
//        return new MailReceiver(mailIn);
//    }
}
