package com.crc.healthmetrics.util;

import org.springframework.core.io.ClassPathResource;

import java.io.*;

public class ResourceExtractor {

    private ResourceExtractor() {
        throw new IllegalStateException("Utility class");
    }

    //To extract the handle.exe from the jar
    public static File extractResourceToTempFile(String resourcePath) throws IOException {
        ClassPathResource resource = new ClassPathResource(resourcePath);
        if (!resource.exists()) {
            throw new FileNotFoundException("Resource not found: " + resourcePath);
        }

        File tempFile = File.createTempFile("handle", ".exe");
        tempFile.deleteOnExit();

        try (InputStream inputStream = resource.getInputStream();
             FileOutputStream outputStream = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            throw new IOException("Error while extracting resource to temporary file", e);
        }

        return tempFile;
    }
}
