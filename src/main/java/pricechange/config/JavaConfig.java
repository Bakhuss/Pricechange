package pricechange.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Scanner;

@Configuration
public class JavaConfig {
    @Bean
    public HashSet<String> makerFilter() {
        String fileName = "filter.txt";
        HashSet<String> hashSet = new HashSet<>();
        try (InputStream is = new FileInputStream(fileName)) {
            Scanner sc = new Scanner(is, "UTF-8");
            while (sc.hasNext()) {
                hashSet.add(sc.nextLine().trim().toLowerCase());
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Not found property file " + fileName);
        }
        return hashSet;
    }
}
