package pricechange.mail.service.impl;

import com.opencsv.CSVReader;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pricechange.csv.model.CSVIksora;
import pricechange.csv.model.CSVPrice4KITAIAVTORUS;
import pricechange.mail.model.Mail;
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
import javax.mail.internet.MimeUtility;
import javax.mail.search.FlagTerm;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

@Service
public class MailServiceImpl implements MailService {
    private final Logger log = LoggerFactory.getLogger(MailServiceImpl.class);
    private final Mail mail;
    private final HashSet<String> makerFilter;

    @Autowired
    public MailServiceImpl(Mail mail, HashSet<String> makerFilter) {
        this.mail = mail;
        this.makerFilter = makerFilter;
    }

    public void getNewLetters() throws MessagingException {
        Session session = Session.getDefaultInstance(mail.getProperties(), new MailAuthenticator());
        Store store = session.getStore(mail.getProperties().getProperty("mail.store.protocol"));
        store.connect(mail.getProperties().getProperty("mail.host"), null, null);
        Folder inboxFolder = store.getFolder(mail.getProperties().getProperty("mail.inbox.folder"));
        inboxFolder.open(Folder.READ_ONLY);
        System.out.println(inboxFolder.getMessages().length);
        for (Message m : inboxFolder.getMessages()) {
            System.out.println(m.getSubject());
        }
        Message[] newMessages = inboxFolder.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
        System.out.println("new messages");
        for (Message m : newMessages) {
            try {
                if (m.getContent() instanceof Multipart) {
                    Multipart mp = (Multipart) m.getContent();
                    for (int i = 0; i < mp.getCount(); i++) {
                        Part part = mp.getBodyPart(i);

                        if (part.getDisposition() != null) {
                            String fileName = MimeUtility.decodeText(part.getFileName());
                            System.out.println(fileName);

                            if (part.getFileName().equals("Price4KITAIAVTORUS.csv")) {
                                getPartkom(part);
                                fileName = "ПАРТКОМ.csv";
                            }
                            if (fileName.toLowerCase().endsWith(".txt")) {
                                getIksora(part);
                                fileName = "ИКСОРА " + fileName.split(" ")[0] + ".txt";
                            }

                            byte[] bytes = new byte[part.getSize()];
                            part.getInputStream().read(bytes);
                            File priceDir = new File("price");
                            if (!priceDir.exists() || !priceDir.isDirectory()) {
                                priceDir.mkdir();
                            }

                            OutputStream os = new FileOutputStream(priceDir + "/" + fileName);
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
        System.out.println("count: " + newMessages.length);
        store.close();

        log.info(mail.toString());
    }

    private class MailAuthenticator extends Authenticator {
        public PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(mail.getUsername(), mail.getPassword());
        }
    }

    private ColumnPositionMappingStrategy setColumMappingPartkom() {
        ColumnPositionMappingStrategy strategy = new ColumnPositionMappingStrategy();
        strategy.setType(CSVPrice4KITAIAVTORUS.class);
        String[] columns = new String[]{"number", "model", "name", "field4", "field5", "field6", "field7"};
        strategy.setColumnMapping(columns);
        return strategy;
    }

    private ColumnPositionMappingStrategy setColumMappingIksora() {
        ColumnPositionMappingStrategy strategy = new ColumnPositionMappingStrategy();
        strategy.setType(CSVIksora.class);
        String[] columns = new String[]{"makerName", "detailNumber", "detailName", "quantity", "outputPrice", "partyCount"};
        strategy.setColumnMapping(columns);
        return strategy;
    }

    private void getPartkom(Part part) throws IOException, MessagingException {
        Reader reader = new InputStreamReader(part.getInputStream(), StandardCharsets.UTF_8);
        CSVReader csvReader = new CSVReader(reader, ';');
        CsvToBean<CSVPrice4KITAIAVTORUS> bean = new CsvToBean<>();
        bean.setCsvReader(csvReader);
        bean.setMappingStrategy(setColumMappingPartkom());
        List<CSVPrice4KITAIAVTORUS> listPrice = bean.parse();

        System.out.println(listPrice.size());
        List<CSVPrice4KITAIAVTORUS> filteredPrice = listPrice.stream()
                .filter(p -> makerFilter.contains(p.getModel().trim().toLowerCase()))
                .collect(Collectors.toList());
        System.out.println("filtered price size: " + filteredPrice.size());

        filteredPrice.stream()
                .collect(toMap(CSVPrice4KITAIAVTORUS::getModel, p -> p, (p, q) -> p)).values()
                .forEach(System.out::println);
    }

    private void getIksora(Part part) throws IOException, MessagingException {
        Reader reader = new InputStreamReader(part.getInputStream(), StandardCharsets.UTF_8);
        CSVReader csvReader = new CSVReader(reader, ';');
        CsvToBean<CSVIksora> bean = new CsvToBean<>();
        bean.setCsvReader(csvReader);
        bean.setMappingStrategy(setColumMappingIksora());
        List<CSVIksora> listPrice = bean.parse();

        System.out.println(listPrice.size());
        List<CSVIksora> filteredPrice = listPrice.stream()
                .filter(p -> makerFilter.contains(p.getMakerName().trim().toLowerCase()))
                .collect(Collectors.toList());
        System.out.println("filtered price size: " + filteredPrice.size());

        filteredPrice.stream()
                .collect(toMap(CSVIksora::getMakerName, p -> p, (p, q) -> p)).values()
                .forEach(System.out::println);
    }
}
