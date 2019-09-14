package pricechange.file.service;

import pricechange.mail.model.FileByBytes;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class File {
    public void save(FileByBytes fileByBytes) throws IOException {
        java.io.File priceDir = new java.io.File("price");
        if (!priceDir.exists() || !priceDir.isDirectory()) {
            priceDir.mkdir();
        }
        OutputStream os = new FileOutputStream(priceDir + "/" + fileByBytes.getName());
        OutputStream bos = new BufferedOutputStream(os);
        bos.write(fileByBytes.getBytes());
        bos.close();
        os.close();
    }
}
