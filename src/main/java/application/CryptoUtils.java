package application;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;

public class CryptoUtils {
    private static final int SALT_LEN = 16;
    private static final int IV_LEN = 12;
    private static final int ITERATIONS = 65536;
    private static final int KEY_BITS = 128;

    public static Path encryptFile(Path inputPath, String password) throws Exception {
        byte[] input = Files.readAllBytes(inputPath);

        SecureRandom rnd = new SecureRandom();
        byte[] salt = new byte[SALT_LEN];
        rnd.nextBytes(salt);
        byte[] iv = new byte[IV_LEN];
        rnd.nextBytes(iv);

        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_BITS);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = skf.generateSecret(spec).getEncoded();
        SecretKey key = new SecretKeySpec(keyBytes, "AES");

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);
        byte[] cipherText = cipher.doFinal(input);

        byte[] outBytes = new byte[salt.length + iv.length + cipherText.length];
        System.arraycopy(salt, 0, outBytes, 0, salt.length);
        System.arraycopy(iv, 0, outBytes, salt.length, iv.length);
        System.arraycopy(cipherText, 0, outBytes, salt.length + iv.length, cipherText.length);

        Path outDir = Path.of(System.getProperty("user.dir"), "generated");
        Files.createDirectories(outDir);
        Path outPath = outDir.resolve(inputPath.getFileName().toString() + ".enc");
        Files.write(outPath, outBytes);
        return outPath;
    }

    public static Path decryptFile(Path encPath, String password) throws Exception {
        byte[] data = Files.readAllBytes(encPath);
        if (data.length < SALT_LEN + IV_LEN) throw new IllegalArgumentException("Invalid encrypted file");

        byte[] salt = new byte[SALT_LEN];
        System.arraycopy(data, 0, salt, 0, SALT_LEN);
        byte[] iv = new byte[IV_LEN];
        System.arraycopy(data, SALT_LEN, iv, 0, IV_LEN);
        byte[] ct = new byte[data.length - SALT_LEN - IV_LEN];
        System.arraycopy(data, SALT_LEN + IV_LEN, ct, 0, ct.length);

        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_BITS);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = skf.generateSecret(spec).getEncoded();
        SecretKey key = new SecretKeySpec(keyBytes, "AES");

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);
        byte[] plain = cipher.doFinal(ct);

        Path outDir = Path.of(System.getProperty("user.dir"), "generated");
        Files.createDirectories(outDir);
        String name = encPath.getFileName().toString();
        String outName = name.endsWith(".enc") ? name.substring(0, name.length() - 4) : (name + ".dec");
        Path outPath = outDir.resolve(outName);
        Files.write(outPath, plain);
        return outPath;
    }
}
