package pricechange.mail.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pricechange.mail.Mail;
import pricechange.mail.service.MailService;

import javax.mail.Authenticator;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.FlagTerm;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

@Service
public class MailServiceImpl implements MailService {
    private final Logger log = LoggerFactory.getLogger(MailServiceImpl.class);
    private final Mail mail;

    @Autowired
    public MailServiceImpl(Mail mail) {
        this.mail = mail;
    }

    public void getNewLetters() throws MessagingException {
        Session session = Session.getDefaultInstance(mail.getProperties(), new MailAuthenticator());
        Store store = session.getStore(mail.getProperties().getProperty("mail.store.protocol"));
        store.connect(mail.getProperties().getProperty("mail.host"), null, null);
        Folder freelancer = store.getFolder(mail.getProperties().getProperty("mail.inbox.folder"));
        freelancer.open(Folder.READ_ONLY);
        System.out.println(freelancer.getMessages().length);
        for (Message m : freelancer.getMessages()) {
            System.out.println(m.getSubject());
        }
        Message[] search = freelancer.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
        for (Message m : search) {
            try {
                if (m.getContent() instanceof Multipart) {
                    Multipart mp = (Multipart) m.getContent();
                    for (int i = 0; i < mp.getCount(); i++) {
                        Part part = mp.getBodyPart(i);

                        if (part.getDisposition() != null) {
                            System.out.println(part.getFileName());

                            byte[] bytes = new byte[part.getSize()];
                            part.getInputStream().read(bytes);
                            File priceDir = new File("price");
                            if (!priceDir.exists() || !priceDir.isDirectory()) {
                                priceDir.mkdir();
                            }
                            OutputStream os = new FileOutputStream(priceDir + "/" + m.getSubject());
                            BufferedOutputStream bos = new BufferedOutputStream(os);
                            bos.write(bytes);
                            bos.close();
                            os.close();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("count: " + search.length);
        store.close();

        log.info(mail.toString());
    }

    private class MailAuthenticator extends Authenticator {
        public PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(mail.getUsername(), mail.getPassword());
        }
    }
}
