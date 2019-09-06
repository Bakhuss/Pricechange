package pricechange.mail.service.impl;

import com.opencsv.CSVReader;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pricechange.csv.model.CSVPrice;
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
import javax.mail.search.FlagTerm;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

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
                            if (part.getFileName().equals("Price4KITAIAVTORUS.csv")) {
                                Reader reader = new InputStreamReader(part.getInputStream(), StandardCharsets.UTF_8);
                                CSVReader csvReader = new CSVReader(reader, ';');
                                System.out.println("-----------------------");
                                CsvToBean<CSVPrice> bean = new CsvToBean<>();
                                bean.setCsvReader(csvReader);
                                bean.setMappingStrategy(setColumMapping());
                                List<CSVPrice> listPrice = bean.parse();
                                HashSet<String> hashSet = new HashSet<>();
                                InputStream ras = new FileInputStream("filter.txt");
                                Scanner sc = new Scanner(ras, "UTF-8");
                                while (sc.hasNext()) {
                                    hashSet.add(sc.nextLine().trim().toLowerCase());
                                }
                                System.out.println("hashSet: " + hashSet.size());

                                System.out.println(listPrice.size());
                                List<CSVPrice> filteredPrice = listPrice.stream()
                                        .filter(p -> hashSet.contains(p.getModel().trim().toLowerCase()))
                                        .collect(Collectors.toList());
                                System.out.println("filtered price size: " + filteredPrice.size());

                                filteredPrice.stream()
                                        .collect(toMap(CSVPrice::getModel, p -> p, (p, q) -> p)).values()
                                        .forEach(System.out::println);
                                System.out.println("-----------------------");
                            }

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

    private ColumnPositionMappingStrategy setColumMapping() {
        ColumnPositionMappingStrategy strategy = new ColumnPositionMappingStrategy();
        strategy.setType(CSVPrice.class);
        String[] columns = new String[]{"number", "model", "name", "field4", "field5", "field6", "field7"};
        strategy.setColumnMapping(columns);
        return strategy;
    }
}
