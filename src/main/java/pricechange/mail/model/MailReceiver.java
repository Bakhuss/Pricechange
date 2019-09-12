package pricechange.mail.model;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.FlagTerm;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MailReceiver {
    private Mail mailIn;

    public MailReceiver(Mail mailIn) {
        this.mailIn = mailIn;
    }

    public List<Message> receiveMessage() throws MessagingException {
        Session session = Session.getDefaultInstance(
                mailIn.getProperties(),
                new MailAuthenticator(mailIn.getUsername(), mailIn.getPassword())
        );
        Store store = session.getStore(mailIn.getProperties().getProperty("mail.store.protocol"));
        store.connect(mailIn.getProperties().getProperty("mail.host"), null, null);
        Folder inboxFolder = store.getFolder(mailIn.getProperties().getProperty("mail.inbox.folder"));
        inboxFolder.open(Folder.READ_ONLY);
        Message[] newMessages = inboxFolder.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
        store.close();
        return new ArrayList<>(Arrays.asList(newMessages));
    }
}
