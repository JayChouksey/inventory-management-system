package com.example.coditas.common.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;

public class ExcelFileUtil {

    public static MultipartFile getFile(String path) {
        try {
            File file = new File(path);
            byte[] content = Files.readAllBytes(file.toPath());
            return new MultipartFile() {
                @Override
                public String getName() { return file.getName(); }

                @Override
                public String getOriginalFilename() { return file.getName(); }

                @Override
                public String getContentType() { return "application/octet-stream"; }

                @Override
                public boolean isEmpty() { return content.length == 0; }

                @Override
                public long getSize() { return content.length; }

                @Override
                public byte[] getBytes() throws IOException { return content; }

                @Override
                public java.io.InputStream getInputStream() throws IOException { return new FileInputStream(file); }

                @Override
                public void transferTo(File dest) throws IOException { Files.write(dest.toPath(), content); }
            };
        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + path, e);
        }
    }
}