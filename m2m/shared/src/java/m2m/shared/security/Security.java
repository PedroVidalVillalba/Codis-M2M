package m2m.shared.security;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.*;
import java.rmi.Remote;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class Security {
    /* Descripción de los algoritmos y constantes de seguridad usadas */
    public static final String AES_ALGORITHM = "AES";
    public static final String CIPHER_ALGORITHM = "AES/GCM/NoPadding";
    public static final String SHA3_ALGORITHM = "SHA3-256";
    public static final String KEY_EXCHANGE_ALGORITHM = "X25519";   // Elliptic Curve Diffie-Hellman Ephemeral; ver RFC 7778
    public static final String HMAC_SHA3_ALGORITHM = "HmacSHA3-256";
    public static final String SIGN_ALGORITHM = "Ed25519";
    public static final int GCM_IV_LENGTH = 12;     // 96 bits
    public static final int GCM_TAG_LENGTH = 128;   // 128 bits
    public static final int AES_KEY_SIZE = 256;     // Bits para usar en AES
    public static final int NONCE_LENGTH = 16;      // 128 bits
    public static final int DIGEST_LENGTH = 32;     // 256 bits
    public static final String HKDF_INFO = "M2M key derivation for AES";

    public record Ephemeral(PublicKey publicKey, PrivateKey privateKey, byte[] nonce) {}

    private final Map<Remote, SecretKey> keys;
    private final Map<Remote, Ephemeral> ongoingHandshakes;
    private Remote selfReference;

    public Security() {
        this.keys = new HashMap<>();
        this.ongoingHandshakes = new HashMap<>();
    }

    public void setSelfReference(Remote selfReference) {
        this.selfReference = selfReference;
    }

    /* Método encrypt de bajo nivel actuando sobre arrays de bytes directamente */
    public static byte[] encrypt(byte[] data, SecretKey key) throws GeneralSecurityException {
        ensureNotNull(data, key);
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);

        byte[] iv = generateIV();   // Generar un nuevo Initialization Vector para cada encriptación
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);

        byte[] encryptedData = cipher.doFinal(data);
        return combine(iv, encryptedData);   // Prepend IV a los datos encriptados
    }

    public OutputStream encryptStream(OutputStream out, Remote receiver) throws GeneralSecurityException, IOException {
        ensureNotNull(out, receiver);
        SecretKey key = getSecretKey(receiver);
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);

        byte[] iv = generateIV();   // Generar un nuevo Initialization Vector para cada encriptación
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);

        /* Escribir el Initialization Vector en el stream primero */
        out.write(iv);
        /* Escribir el hash del emisor para poder conocerlo en el receptor */
        out.write(digest(serialize(selfReference), iv));
        out.flush();

        return new CipherOutputStream(out, cipher);
    }

    public String encrypt(String data, Remote remote) throws GeneralSecurityException {
        ensureNotNull(data, remote);
        SecretKey key = getSecretKey(remote);
        return Base64.getEncoder().encodeToString(encrypt(data.getBytes(), key));
    }

    public SecretKey encrypt(SecretKey secretKey, Remote remote) throws GeneralSecurityException {
        ensureNotNull(secretKey, remote);
        SecretKey key = getSecretKey(remote);
        return new SecretKeySpec(encrypt(secretKey.getEncoded(), key), AES_ALGORITHM);
    }

    public byte[] encrypt(byte[] data, Remote remote) throws GeneralSecurityException {
        ensureNotNull(data, remote);
        SecretKey key = getSecretKey(remote);
        return encrypt(data, key);
    }

    /* Método encrypt de bajo nivel actuando sobre arrays de bytes directamente */
    public static byte[] decrypt(byte[] data, SecretKey key) throws GeneralSecurityException {
        ensureNotNull(data, key);
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);

        byte[] iv = extractIV(data);
        byte[] encryptedData = extractEncryptedData(data);

        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);

        return cipher.doFinal(encryptedData);
    }

    public InputStream decryptStream(InputStream in, SecureSocket socket) throws GeneralSecurityException, IOException {
        ensureNotNull(in);
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);

        /* Leer el Initialization Vector del stream primero */
        byte[] iv = new byte[GCM_IV_LENGTH];
        if (in.read(iv) != GCM_IV_LENGTH) {
            throw new GeneralSecurityException("No se pudo leer el IV del stream.");
        }

        /* Leer el hash del emisor para poder inicializarlo */
        byte[] senderHash = new byte[DIGEST_LENGTH];
        if (in.read(senderHash) != DIGEST_LENGTH) {
            throw new GeneralSecurityException("No se pudo leer el hash del emisor del stream");
        }

        /* Buscar una referencia conocida con hash coincidente */
        Remote sender = null;
        for (Remote remote : keys.keySet()) {
            if (Arrays.equals(senderHash, digest(serialize(remote), iv))) {
                sender = remote;
                break;
            }
        }
        if (sender == null) {
            throw new GeneralSecurityException("El hash del emisor no coincide con ninguna referencia conocida");
        }

        /* Inicializar el descifrado */
        SecretKey key = getSecretKey(sender);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);

        /* Actualizar el socket para que sepa a quién responder */
        socket.setReceiver(sender);

        return new CipherInputStream(in, cipher);
    }

    public String decrypt(String data, Remote remote) throws GeneralSecurityException {
        ensureNotNull(data, remote);
        SecretKey key = getSecretKey(remote);
        byte[] decodedData = Base64.getDecoder().decode(data);
        return new String(decrypt(decodedData, key));
    }

    public SecretKey decrypt(SecretKey encodedKey, Remote remote) throws GeneralSecurityException {
        ensureNotNull(encodedKey, remote);
        SecretKey key = getSecretKey(remote);
        return new SecretKeySpec(decrypt(encodedKey.getEncoded(), key),AES_ALGORITHM);
    }

    public byte[] decrypt(byte[] data, Remote remote) throws GeneralSecurityException {
        ensureNotNull(data, remote);
        SecretKey key = getSecretKey(remote);
        return decrypt(data, key);
    }

    public static String digest(String data, String salt) throws GeneralSecurityException {
        ensureNotNull(data, salt);
        byte[] hash = digest(data.getBytes(), salt.getBytes());
        return Base64.getEncoder().encodeToString(hash);
    }

    /* Método digest de bajo nivel actuando sobre arrays de bytes directamente */
    public static byte[] digest(byte[] data, byte[] salt) throws GeneralSecurityException {
        ensureNotNull(data, salt);
        MessageDigest digest = MessageDigest.getInstance(SHA3_ALGORITHM);
        digest.update(salt);
        return digest.digest(data);
    }

    public static Ephemeral generateEphemeral() throws GeneralSecurityException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(KEY_EXCHANGE_ALGORITHM);
        KeyPair keyPair = keyGen.generateKeyPair();
        byte[] nonce = generateNonce();
        return new Ephemeral(keyPair.getPublic(), keyPair.getPrivate(), nonce);
    }

    /** Genera un par efímero de clave pública-clave privada, y un nonce y los asocia a {@code remote} */
    public synchronized Ephemeral generateEphemeral(Remote remote) throws GeneralSecurityException {
        ensureNotNull(remote);
        Ephemeral ephemeral = generateEphemeral();
        ongoingHandshakes.put(remote, ephemeral);
        return ephemeral;
    }

    public synchronized Ephemeral getEphemeral(Remote remote) throws GeneralSecurityException {
        ensureNotNull(remote);

        Ephemeral ephemeral = ongoingHandshakes.remove(remote);
        if (ephemeral == null) {
            throw new GeneralSecurityException("Datos efímeros de seguridad no encontrados para " + remote);
        }
        return ephemeral;
    }

    public static byte[] computeSharedSecret(PrivateKey privateKey, PublicKey publicKey) throws GeneralSecurityException {
        ensureNotNull(privateKey, publicKey);

        KeyAgreement keyAgreement = KeyAgreement.getInstance(KEY_EXCHANGE_ALGORITHM);
        keyAgreement.init(privateKey);
        keyAgreement.doPhase(publicKey, true);
        return keyAgreement.generateSecret();
    }

    public static SecretKey deriveSecretKey(byte[] sharedSecret, byte[] salt) throws GeneralSecurityException {
        ensureNotNull(sharedSecret, salt);

        /* HKDF (HMAC-based Key Derivation Function) para derivar una clave para AES */
        Mac hmac = Mac.getInstance(HMAC_SHA3_ALGORITHM);
        SecretKeySpec secretKeySpec = new SecretKeySpec(sharedSecret, HMAC_SHA3_ALGORITHM);

        hmac.init(secretKeySpec);
        hmac.update(salt);
        byte[] prk = hmac.doFinal();    /* Pseudo-random key */

        byte[] expandedKey = hkdfExpand(prk);
        return new SecretKeySpec(expandedKey, AES_ALGORITHM);
    }

    public synchronized void storeSecretKey(Remote remote, SecretKey secretKey) {
        ensureNotNull(remote, secretKey);
        keys.put(remote, secretKey);
    }

    public synchronized void removeSecretKey(Remote remote) {
        ensureNotNull(remote);
        keys.remove(remote);
    }

    public synchronized SecretKey getSecretKey(Remote remote) throws GeneralSecurityException {
        ensureNotNull(remote);
        SecretKey key = keys.get(remote);
        if (key == null) {
            throw new GeneralSecurityException("Ninguna clave secreta asociada al objeto remoto " + remote);
        }
        return key;
    }

    public static PublicKey loadPublicKey(String resourcePath) throws GeneralSecurityException, IOException {
        ensureNotNull(resourcePath);
        try (InputStream inputStream = Security.class.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IOException("Public key resource not found: " + resourcePath);
            }
            byte[] publicKeyBytes = extractPemKey(inputStream.readAllBytes());
            X509EncodedKeySpec spec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(SIGN_ALGORITHM);
            return keyFactory.generatePublic(spec);
        }
    }

    public static PrivateKey loadPrivateKey(String resourcePath) throws GeneralSecurityException, IOException {
        ensureNotNull(resourcePath);
        try (InputStream inputStream = Security.class.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IOException("Private key resource not found: " + resourcePath);
            }
            byte[] privateKeyBytes = extractPemKey(inputStream.readAllBytes());
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(privateKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(SIGN_ALGORITHM);
            return keyFactory.generatePrivate(spec);
        }
    }

    public static byte[] sign(byte[] data, PrivateKey privateKey) throws GeneralSecurityException {
        ensureNotNull(data, privateKey);
        Signature signature = Signature.getInstance(SIGN_ALGORITHM);
        signature.initSign(privateKey);
        signature.update(data);
        return signature.sign();
    }

    public static boolean verifySignature(byte[] data, byte[] signedData, PublicKey publicKey) throws GeneralSecurityException {
        ensureNotNull(data, signedData, publicKey);
        Signature signature = Signature.getInstance(SIGN_ALGORITHM);
        signature.initVerify(publicKey);
        signature.update(data);
        return signature.verify(signedData);
    }

    public static SecretKey generateAuthenticationKey() throws GeneralSecurityException {
        KeyGenerator generator = KeyGenerator.getInstance(AES_ALGORITHM);
        SecureRandom random = SecureRandom.getInstanceStrong();
        generator.init(AES_KEY_SIZE, random);
        return generator.generateKey();
    }

    public static byte[] generateNonce() {
        byte[] nonce = new byte[NONCE_LENGTH];
        new SecureRandom().nextBytes(nonce);
        return nonce;
    }

    public static byte[] extractNonce(byte[] combinedData) {
        byte[] nonce = new byte[NONCE_LENGTH];
        System.arraycopy(combinedData, 0, nonce, 0, NONCE_LENGTH);
        return nonce;
    }

    public static byte[] removeNonce(byte[] combinedData) {
        byte[] data = new byte[combinedData.length - NONCE_LENGTH];
        System.arraycopy(combinedData, NONCE_LENGTH, data, 0, data.length);
        return data;
    }

    public static void ensureNotNull(Object... objects) throws IllegalArgumentException {
        for (Object object : objects) {
            if (object == null) {
                throw new IllegalArgumentException("Algún argumento nulo");
            }
        }
    }

    public static byte[] combine(byte[]... arrays) throws GeneralSecurityException {
        ensureNotNull((Object[]) arrays);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        for (byte[] array : arrays) {
            try {
                outputStream.write(array);
            } catch (IOException e) {
                throw new GeneralSecurityException("Error al combinar arrays", e);
            }
        }
        return outputStream.toByteArray();
    }

    public static byte[] serialize(Object... objects) throws IOException {
        ensureNotNull(objects);
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try (ObjectOutputStream objectStream = new ObjectOutputStream(byteStream)) {
            for (Object object : objects) {
                objectStream.writeObject(object);
            }
        }
        return byteStream.toByteArray();
    }

    /* Métodos privados */
    private static byte[] generateIV() {
        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    private static byte[] extractIV(byte[] combinedData) {
        byte[] iv = new byte[GCM_IV_LENGTH];
        System.arraycopy(combinedData, 0, iv, 0, GCM_IV_LENGTH);
        return iv;
    }

    private static byte[] extractEncryptedData(byte[] combinedData) {
        byte[] encryptedData = new byte[combinedData.length - GCM_IV_LENGTH];
        System.arraycopy(combinedData, GCM_IV_LENGTH, encryptedData, 0, encryptedData.length);
        return encryptedData;
    }

    private static byte[] extractPemKey(byte[] pemKey) {
        String pemContent = new String(pemKey);

        pemContent = pemContent .replaceAll("-----(BEGIN|END) (PUBLIC|PRIVATE) KEY-----", "")
                                .replaceAll("\\s", "");

        return Base64.getDecoder().decode(pemContent);
    }

    private static byte[] hkdfExpand(byte[] prk) throws GeneralSecurityException {
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
