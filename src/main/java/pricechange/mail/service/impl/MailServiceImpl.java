package pricechange.mail.service.impl;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pricechange.csv.model.CSVIksora;
import pricechange.csv.model.CSVPartkom;
import pricechange.mail.model.FileByBytes;
import pricechange.mail.model.Mail;
import pricechange.mail.model.MailAuthenticator;
import pricechange.mail.service.MailService;

import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.mail.search.FlagTerm;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

@Service
public class MailServiceImpl implements MailService {
    private final Logger log = LoggerFactory.getLogger(MailServiceImpl.class);
    private final Mail mailIn;
    private final Mail mailOut;
    private final HashSet<String> makerFilter;

    @Autowired
    public MailServiceImpl(Mail mailIn, Mail mailOut, HashSet<String> makerFilter) {
        this.mailIn = mailIn;
        this.mailOut = mailOut;
        this.makerFilter = makerFilter;
    }

    public List<FileByBytes> receiveMessage() throws MessagingException {
        List<FileByBytes> fileByBytes = new ArrayList<>();
        Session session = Session.getDefaultInstance(
                mailIn.getProperties(),
                new MailAuthenticator(mailIn.getUsername(), mailIn.getPassword())
        );
        Store store = session.getStore(mailIn.getProperties().getProperty("mail.store.protocol"));
        store.connect(mailIn.getProperties().getProperty("mail.host"), null, null);
        Folder inboxFolder = store.getFolder(mailIn.getProperties().getProperty("mail.inbox.folder"));
        inboxFolder.open(Folder.READ_ONLY);
        System.out.println(inboxFolder.getMessages().length);
        for (Message m : inboxFolder.getMessages()) {
            System.out.println(m.getSubject());
        }
        Message[] newMessages = inboxFolder.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
        System.out.println("new messages");
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

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
                                List<CSVPartkom> partkom = getPartkom(part);
                                System.out.println("filtered price size: " + partkom.size());
                                fileName = "ПАРТКОМ.csv";
                            }
                            if (fileName.toLowerCase().endsWith(".txt")) {
                                List<CSVIksora> iksora = getIksora(part);
                                System.out.println("filtered price size: " + iksora.size());
                                fileName = "ИКСОРА " + fileName.split(" ")[0] + ".csv";
                            }
                            final String name = fileName;

                            executorService.submit(() -> {
                                byte[] bytes;
                                try {
                                    bytes = new byte[part.getSize()];
                                    part.getInputStream().read(bytes);
                                    FileByBytes fbb = new FileByBytes();
                                    fbb.setName(name);
                                    fbb.setBytes(bytes);
                                    fileByBytes.add(fbb);
                                } catch (MessagingException | IOException e) {
                                    e.printStackTrace();
                                }
                            });

//                            byte[] bytes = new byte[part.getSize()];
//                            part.getInputStream().read(bytes);
//                            FileByBytes fbb = new FileByBytes();
//                            fbb.setName(fileName);
//                            fbb.setBytes(bytes);
//                            fileByBytes.add(fbb);

//                            File file = new File();
//                            file.save(fbb);

//                            File priceDir = new File("price");
//                            if (!priceDir.exists() || !priceDir.isDirectory()) {
//                                priceDir.mkdir();
//                            }
//
//                            OutputStream os = new FileOutputStream(priceDir + "/" + fileName);
//                            OutputStream bos = new BufferedOutputStream(os);
//                            bos.write(bytes);
//                            bos.close();
//                            os.close();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        executorService.shutdown();
        System.out.println("exec.serv.isShutdown: " + executorService.isShutdown());
        System.out.println("exec.serv.isTerminated: " + executorService.isTerminated());
        while (!executorService.isTerminated()) {

        }
        store.close();
        return fileByBytes;
    }

    public void sendMessage(String fileName) throws MessagingException {
        Session session = Session.getDefaultInstance(
                mailOut.getProperties(),
                new MailAuthenticator("ali.da", mailOut.getPassword())
        );
        InternetAddress emailFrom = new InternetAddress(mailOut.getUsername());
        InternetAddress emailTo = new InternetAddress(mailOut.getUsername());
        String reply = mailOut.getProperties().getProperty("mail.out.reply.to");
        InternetAddress replyTo = (reply != null) ? new InternetAddress(reply) : null;
        MimeMessage message = new MimeMessage(session);
        message.setFrom(emailFrom);
        message.setRecipient(Message.RecipientType.TO, emailTo);
        message.setSubject(fileName);
        if (replyTo != null) message.setReplyTo(new Address[]{replyTo});
        FileDataSource fds = new FileDataSource(fileName);
        Multipart mp = new MimeMultipart();
        MimeBodyPart bodyPart = new MimeBodyPart();
        bodyPart.setContent("text", "text/plain; charset=utf-8");
        mp.addBodyPart(bodyPart);
        message.setContent(mp);
        Transport.send(message);
    }

    private List<CSVPartkom> getPartkom(Part part) throws IOException, MessagingException {
        Reader reader = new InputStreamReader(part.getInputStream(), StandardCharsets.UTF_8);
        CSVReader csvReader = new CSVReaderBuilder(reader)
                .withCSVParser(new CSVParserBuilder().withSeparator(';').build())
                .build();

        CsvToBean<CSVPartkom> bean = new CsvToBean<>();
        bean.setCsvReader(csvReader);
        bean.setMappingStrategy(setColumMappingPartkom());
        List<CSVPartkom> listPrice = bean.parse();

        System.out.println(listPrice.size());
        List<CSVPartkom> filteredPrice = listPrice.stream()
                .filter(p -> makerFilter.contains(p.getModel().trim().toLowerCase()))
                .collect(Collectors.toList());

        filteredPrice.stream()
                .collect(toMap(CSVPartkom::getModel, p -> p, (p, q) -> p)).values()
                .forEach(System.out::println);
        return filteredPrice;
    }

    private ColumnPositionMappingStrategy setColumMappingPartkom() {
        ColumnPositionMappingStrategy strategy = new ColumnPositionMappingStrategy();
        strategy.setType(CSVPartkom.class);
        String[] columns = new String[]{"number", "model", "name", "field4", "field5", "field6", "field7"};
        strategy.setColumnMapping(columns);
        return strategy;
    }

    private List<CSVIksora> getIksora(Part part) throws IOException, MessagingException {
        Reader reader = new InputStreamReader(part.getInputStream(), StandardCharsets.UTF_8);
        CSVReader csvReader = new CSVReaderBuilder(reader)
                .withCSVParser(new CSVParserBuilder().withSeparator(';').build())
                .build();

        CsvToBean<CSVIksora> bean = new CsvToBean<>();
        bean.setCsvReader(csvReader);
        bean.setMappingStrategy(setColumMappingIksora());
        List<CSVIksora> listPrice = bean.parse();

        System.out.println(listPrice.size());
        List<CSVIksora> filteredPrice = listPrice.stream()
                .filter(p -> makerFilter.contains(p.getMakerName().trim().toLowerCase()))
                .collect(Collectors.toList());

        filteredPrice.stream()
                .collect(toMap(CSVIksora::getMakerName, p -> p, (p, q) -> p)).values()
                .forEach(System.out::println);
        return filteredPrice;
    }

    private ColumnPositionMappingStrategy setColumMappingIksora() {
        ColumnPositionMappingStrategy strategy = new ColumnPositionMappingStrategy();
        strategy.setType(CSVIksora.class);
        String[] columns = new String[]{"makerName", "detailNumber", "detailName", "quantity", "outputPrice", "partyCount"};
        strategy.setColumnMapping(columns);
        return strategy;
    }
}
