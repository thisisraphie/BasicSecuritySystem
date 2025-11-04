package application;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.util.*;

public class FileEncrypt {

    private static final int SALT_LEN = 16;
    private static final int IV_LEN = 12;
    private static final int KEY_LEN = 256;
    private static final int ITERATIONS = 100_000;

    private static final SecureRandom random = new SecureRandom();

    private static SecretKey deriveKey(char[] password, byte[] salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LEN);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }

    public static void encryptFile(Path input, Path output, char[] password) throws Exception {
        byte[] salt = new byte[SALT_LEN];
        byte[] iv = new byte[IV_LEN];
        random.nextBytes(salt);
        random.nextBytes(iv);

        SecretKey key = deriveKey(password, salt);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv));

        try (FileOutputStream fos = new FileOutputStream(output.toFile());
             CipherOutputStream cos = new CipherOutputStream(fos, cipher);
             FileInputStream fis = new FileInputStream(input.toFile())) {

            fos.write(salt);
            fos.write(iv);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                cos.write(buffer, 0, bytesRead);
            }
        }
    }

    public static void decryptFile(Path input, Path output, char[] password) throws Exception {
        byte[] salt = new byte[SALT_LEN];
        byte[] iv = new byte[IV_LEN];

        try (FileInputStream fis = new FileInputStream(input.toFile())) {
            fis.read(salt);
            fis.read(iv);

            SecretKey key = deriveKey(password, salt);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv));

            try (CipherInputStream cis = new CipherInputStream(fis, cipher);
                 FileOutputStream fos = new FileOutputStream(output.toFile())) {

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = cis.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }
        }
    }
}
