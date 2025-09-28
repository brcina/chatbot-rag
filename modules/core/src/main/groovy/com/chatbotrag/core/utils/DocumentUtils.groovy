package com.chatbotrag.core.utils

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class DocumentUtils {

    static String calculateContentHash(String content) {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        // Content normalisieren vor Hashing
        String normalizedContent = content
                .replaceAll("\\s+", " ")           // Multiple Leerzeichen → single space
                .replaceAll("\\r\\n|\\r|\\n", " ") // Zeilenumbrüche → space
                .trim()
                .toLowerCase();

        byte[] hash = digest.digest(normalizedContent.getBytes(StandardCharsets.UTF_8) as byte[]);
        return bytesToHex(hash);
    }

    static String calculateFileHash(Path filePath) {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream is = Files.newInputStream(filePath)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
        }
        return bytesToHex(digest.digest());
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
