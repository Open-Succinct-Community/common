package com.venky.core.security;

import com.venky.cache.Cache;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class Crypt {
    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null){
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    String provider;
    private Crypt(String provider){
        this.provider = provider;
    }
    static Cache<String,Crypt> cachedInstancesByProvider = new Cache<String, Crypt>() {
        @Override
        protected Crypt getValue(String provider) {
            return new Crypt(provider);
        }
    };
    public static Crypt getInstance(String provider) {
        return cachedInstancesByProvider.get(provider);
    }
    public static Crypt getInstance() {
        return getInstance(BouncyCastleProvider.PROVIDER_NAME);
    }


    public String getBase64Encoded(Key key){
        byte[] encoded = key.getEncoded();
        String b64Key = Base64.getEncoder().encodeToString(encoded);
        return b64Key;
    }
    public SecretKey getSecretKey(String algo,String base64Key){
        return new SecretKeySpec(Base64.getDecoder().decode(base64Key),algo);
    }

    public PublicKey getPublicKey(String algo,String base64PublicKey){
        byte [] binCpk = Base64.getDecoder().decode(base64PublicKey);
        X509EncodedKeySpec pkSpec = new X509EncodedKeySpec(binCpk);

        try {
            KeyFactory keyFactory = KeyFactory.getInstance(algo,provider);
            PublicKey pKey = keyFactory.generatePublic(pkSpec);
            return pKey;
        }catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeySpecException ex){
            throw new RuntimeException(ex);
        }
    }
    public PrivateKey getPrivateKey(String algo,String base64PrivateKey){
        byte [] binCpk = Base64.getDecoder().decode(base64PrivateKey);
        PKCS8EncodedKeySpec pkSpec = new PKCS8EncodedKeySpec(binCpk);

        try {
            KeyFactory keyFactory = KeyFactory.getInstance(algo,provider);
            PrivateKey pKey = keyFactory.generatePrivate(pkSpec);
            return pKey;
        }catch (NoSuchAlgorithmException | NoSuchProviderException |InvalidKeySpecException ex){
            throw new RuntimeException(ex);
        }
    }

    public KeyPair generateKeyPair(String algo,int strength ) {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(algo,provider);
            keyPairGenerator.initialize(strength);
            return keyPairGenerator.generateKeyPair();
        }catch (NoSuchAlgorithmException | NoSuchProviderException ex){
            throw new RuntimeException(ex);
        }
    }


    public static final String KEY_ALGO = "RSA";
    public static final String SIGNATURE_ALGO = "SHA256withRSA";

    public String generateSignature(String payload, String signatureAlgorithm , PrivateKey privateKey)  {
        try {
            Signature signature = Signature.getInstance(signatureAlgorithm, provider); //
            signature.initSign(privateKey);
            signature.update(payload.getBytes());
            return Base64.getEncoder().encodeToString(signature.sign());
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
    public boolean verifySignature(String payload, String signature, String signatureAlgorithm ,PublicKey pKey){
        byte [] data = payload.getBytes();
        byte [] signatureBytes = Base64.getDecoder().decode(signature);
        try {
            Signature s = Signature.getInstance(signatureAlgorithm,provider);
            s.initVerify(pKey);
            s.update(data);
            return s.verify(signatureBytes);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    Cache<String, ThreadLocal<Cipher>> cipherCache = new Cache<String, ThreadLocal<Cipher>>() {
        @Override
        protected ThreadLocal<Cipher> getValue(String algorithm) {
            try {
                Cipher cipher = Cipher.getInstance(algorithm,provider);
                ThreadLocal tl = new ThreadLocal<>();
                tl.set(cipher);
                return tl;
            }catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    };

    public String encrypt(String decrypted, String algorithm, Key key){
        Cipher cipher = cipherCache.get(algorithm).get();
        try {
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encrypted = cipher.doFinal(decrypted.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    public String encrypt(String decrypted, String algorithm, String publicKey){
        try {
            return encrypt(decrypted,algorithm,getPublicKey(algorithm,publicKey));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    public String decrypt(String encrypted, String algorithm , Key key){
        try {
            Cipher cipher = cipherCache.get(algorithm).get();
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encrypted.getBytes(StandardCharsets.UTF_8)));
            return new String(decrypted);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    public String decrypt(String encrypted, String algorithm, String privateKey){
        try {
            return decrypt(encrypted,algorithm,getPrivateKey(algorithm,privateKey));
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }


}
