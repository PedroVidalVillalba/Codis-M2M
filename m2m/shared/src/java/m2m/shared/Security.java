package m2m.shared;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.rmi.Remote;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class Security {
    /* Descripción de los algoritmos y constantes de seguridad usadas */
    public static final String AES_ALGORITHM = "AES/GCM/NoPadding";
    public static final String SHA3_ALGORITHM = "SHA3-256";
    public static final String KEY_EXCHANGE_ALGORITHM = "X25519";   // Elliptic Curve Diffie-Hellman Ephemeral; ver RFC 7778
    public static final String HMAC_SHA3_ALGORITHM = "HmacSHA3-256";
    public static final int GCM_IV_LENGTH = 12;     // 96 bits
    public static final int GCM_TAG_LENGTH = 128;   // 128 bits
    public static final int AES_KEY_SIZE = 256;     // Bits para usar en AES
    public static final String HKDF_INFO = "M2M key derivation for AES";

    private Map<Remote, SecretKey> keys;
    private Map<Remote, KeyPair> ongoingDiffieHellman;

    public Security() {
        this.keys = new HashMap<>();
        this.ongoingDiffieHellman = new HashMap<>();
    }

    public String encrypt(String data, Remote remote) throws GeneralSecurityException {
        SecretKey key = keys.get(remote);
        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);

        byte[] iv = generateIV();   // Generar un nuevo Initialization Vector para cada encriptación
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);

        byte[] encryptedData = cipher.doFinal(data.getBytes());
        byte[] combined = combine(iv, encryptedData);   // Prepend IV a los datos encriptados

        return Base64.getEncoder().encodeToString(combined);
    }

    public String decrypt(String data, Remote remote) throws GeneralSecurityException {
        SecretKey key = keys.get(remote);
        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);

        byte[] decodedData = Base64.getDecoder().decode(data);
        byte[] iv = extractIV(decodedData);
        byte[] encryptedData = extractEncryptedData(decodedData);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);

        byte[] decryptedData = cipher.doFinal(encryptedData);

        return new String(decryptedData);
    }

    public String digest(String data, String salt) throws GeneralSecurityException {
        MessageDigest digest = MessageDigest.getInstance(SHA3_ALGORITHM);
        digest.update(salt.getBytes()); // Incorporar la sal
        byte[] hash = digest.digest(data.getBytes());
        return Base64.getEncoder().encodeToString(hash);
    }

    public KeyPair generateKeyPair() throws GeneralSecurityException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(KEY_EXCHANGE_ALGORITHM);
        return keyGen.generateKeyPair();
    }

    /** Genera un par efímero de clave pública-clave privada, y lo asocia a {@code remote} */
    public KeyPair generateKeyPair(Remote remote) throws GeneralSecurityException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(KEY_EXCHANGE_ALGORITHM);
        KeyPair keyPair = keyGen.generateKeyPair();
        ongoingDiffieHellman.put(remote, keyPair);
        return keyPair;
    }

    public KeyPair getOngoingKeyPair(Remote remote) {
        return ongoingDiffieHellman.get(remote);
    }

    public void removeOngoingKeyPair(Remote remote) {
        ongoingDiffieHellman.remove(remote);
    }

    public byte[] computeSharedSecret(PrivateKey privateKey, PublicKey publicKey) throws GeneralSecurityException {
        KeyAgreement keyAgreement = KeyAgreement.getInstance(KEY_EXCHANGE_ALGORITHM);
        keyAgreement.init(privateKey);
        keyAgreement.doPhase(publicKey, true);
        return keyAgreement.generateSecret();
    }

    public SecretKey deriveAESKey(byte[] sharedSecret, byte[] salt) throws GeneralSecurityException {
        /* HKDF (HMAC-based Key Derivation Function) para derivar una clave para AES */
        Mac hmac = Mac.getInstance(HMAC_SHA3_ALGORITHM);
        SecretKeySpec secretKeySpec = new SecretKeySpec(sharedSecret, HMAC_SHA3_ALGORITHM);

        hmac.init(secretKeySpec);
        hmac.update(salt);
        byte[] prk = hmac.doFinal();    /* Pseudo-random key */

        byte[] expandedKey = hkdfExpand(prk);
        return new SecretKeySpec(expandedKey, AES_ALGORITHM);
    }

    public void addSecretKey(Remote remote, SecretKey secretKey) {
        keys.put(remote, secretKey);
    }

    /* Métodos privados */
    private byte[] generateIV() {
        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    private byte[] combine(byte[] iv, byte[] encryptedData) {
        byte[] combined = new byte[iv.length + encryptedData.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encryptedData, 0, combined, iv.length, encryptedData.length);
        return combined;
    }

    private byte[] extractIV(byte[] combinedData) {
        byte[] iv = new byte[GCM_IV_LENGTH];
        System.arraycopy(combinedData, 0, iv, 0, GCM_IV_LENGTH);
        return iv;
    }

    private byte[] extractEncryptedData(byte[] combinedData) {
        byte[] encryptedData = new byte[combinedData.length - GCM_IV_LENGTH];
        System.arraycopy(combinedData, GCM_IV_LENGTH, encryptedData, 0, encryptedData.length);
        return encryptedData;
    }

    private byte[] hkdfExpand(byte[] prk) throws GeneralSecurityException {
        Mac hmac = Mac.getInstance(HMAC_SHA3_ALGORITHM);
        hmac.init(new SecretKeySpec(prk, HMAC_SHA3_ALGORITHM));

        byte[] info = HKDF_INFO.getBytes();
        byte[] key = new byte[AES_KEY_SIZE / 8];
        byte[] previous = new byte[0];  /* Bloque anterior (empieza con un bloque vacío) */
        byte[] output;
        int blockCount = Math.ceilDiv(AES_KEY_SIZE, 8 * hmac.getMacLength());

        int offset = 0;
        for (int i = 0; i < blockCount; i++) {
            hmac.update(previous);
            hmac.update(info);
            hmac.update((byte) (i + 1));    // Counter byte
            output = hmac.doFinal();

            int bytesToCopy = Math.min(output.length, key.length - offset);
            System.arraycopy(output, 0, key, i * hmac.getMacLength(), bytesToCopy);
            previous = output;
            offset += bytesToCopy;
        }

        return key;
    }
}
