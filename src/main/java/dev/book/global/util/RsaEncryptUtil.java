package dev.book.global.util;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RsaEncryptUtil {
    private final static String PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlS+IEdq+VnsVBltibckkyUu+B8wJwPJO/ogJcPtN84tLUOd6HLC2IUGKBboeZtjAXFiuW5K2gnpxusQ/I7U88ttr0BID6cc4oKEnYx2ucHxTq8KjRV7INRNt3o+VufdOQDsVjFRQebrMi/neZZavi/NZOQP6USnDdmC7P1m9HaCHtP4sbCbRXH5oGCQOndzrRNo+9hXRQhtLX/0NDl+mN0SB2zQvur+VqAGhGMHYYfNOQtRphNnF3Me3ODZTVnSXk0GE/cmIaCgUZH7gZ+SnjO+4xw2XmvMyEXr5inzpPWDUpP+mQw+7NU7RJAVbyqzd0jcLERXJXDwDTDzcHf8a/QIDAQAB";
    public static String encrypt(String content) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(PUBLIC_KEY);

            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(keySpec);

            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encrypted = cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
