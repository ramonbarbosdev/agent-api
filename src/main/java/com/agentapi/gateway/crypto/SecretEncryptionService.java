package com.agentapi.gateway.crypto;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Service;

import com.agentapi.config.SecretsProperties;
import com.agentapi.exception.ApiException;
import com.agentapi.exception.ErrorCode;

@Service
public class SecretEncryptionService {

    private static final String ALGO = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int IV_LENGTH = 12;

    private final SecretKey key;
    private final SecureRandom random = new SecureRandom();

    public SecretEncryptionService(SecretsProperties secretsProperties) {
        this.key = resolveKey(secretsProperties.getEncryptionKey());
    }

    public String encrypt(String plainText) {
        try {
            byte[] iv = new byte[IV_LENGTH];
            random.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(ALGO);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            ByteBuffer buffer = ByteBuffer.allocate(iv.length + cipherText.length);
            buffer.put(iv);
            buffer.put(cipherText);
            return Base64.getEncoder().encodeToString(buffer.array());
        } catch (Exception ex) {
            throw new ApiException(ErrorCode.INTERNAL_ERROR, "Falha ao proteger credencial.");
        }
    }

    public String decrypt(String cipherBlob) {
        try {
            byte[] decoded = Base64.getDecoder().decode(cipherBlob);
            ByteBuffer buffer = ByteBuffer.wrap(decoded);
            byte[] iv = new byte[IV_LENGTH];
            buffer.get(iv);
            byte[] cipherText = new byte[buffer.remaining()];
            buffer.get(cipherText);
            Cipher cipher = Cipher.getInstance(ALGO);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new ApiException(ErrorCode.INTERNAL_ERROR, "Falha ao ler credencial.");
        }
    }

    private static SecretKey resolveKey(String base64Key) {
        if (base64Key == null || base64Key.isBlank()) {
            byte[] dev = new byte[32];
            new SecureRandom().nextBytes(dev);
            return new SecretKeySpec(dev, "AES");
        }
        byte[] raw = Base64.getDecoder().decode(base64Key);
        if (raw.length != 32) {
            throw new IllegalStateException("AGENT_API_SECRETS_KEY deve ser Base64 de 32 bytes.");
        }
        return new SecretKeySpec(raw, "AES");
    }
}
