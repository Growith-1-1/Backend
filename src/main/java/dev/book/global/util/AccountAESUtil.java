package dev.book.global.util;

import dev.book.global.exception.util.AESErrorCode;
import dev.book.global.exception.util.AESErrorException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class AccountAESUtil {

    @Value("${codef.account_key}")
    private String accountKey;

    private final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private final int IV_LENGTH = 16;

    public String encrypt(String plainText) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            SecretKeySpec secretKey = new SecretKeySpec(accountKey.getBytes(), "AES");

            byte[] iv = new byte[IV_LENGTH];
            SecureRandom secureRandom = new SecureRandom();
            secureRandom.nextBytes(iv);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec);

            byte[] encrypted = cipher.doFinal(plainText.getBytes());

            byte[] encryptedWithIv = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, encryptedWithIv, 0, iv.length);
            System.arraycopy(encrypted, 0, encryptedWithIv, iv.length, encrypted.length);

            return Base64.getEncoder().encodeToString(encryptedWithIv);
        } catch (Exception e) {
            throw new AESErrorException(AESErrorCode.FAIL_ENCRYPT, e.getMessage());
        }
    }

    public String decrypt(String encryptedText) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);

            byte[] encryptedIvTextBytes = Base64.getDecoder().decode(encryptedText);

            byte[] iv = new byte[IV_LENGTH];
            byte[] encryptedBytes = new byte[encryptedIvTextBytes.length - IV_LENGTH];
            System.arraycopy(encryptedIvTextBytes, 0, iv, 0, IV_LENGTH);
            System.arraycopy(encryptedIvTextBytes, IV_LENGTH, encryptedBytes, 0, encryptedBytes.length);

            SecretKeySpec secretKey = new SecretKeySpec(accountKey.getBytes(), "AES");
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec);

            // 5. 복호화
            byte[] decrypted = cipher.doFinal(encryptedBytes);

            return new String(decrypted);
        } catch (Exception e) {
            throw new AESErrorException(AESErrorCode.FAIL_DECRYPT, e.getMessage());
        }
    }
}
