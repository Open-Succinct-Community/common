package com.venky.core.security;

import com.venky.cache.Cache;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class Crypt {
    private static Crypt crypt;
    private Crypt(){

    }

    public static Crypt getInstance() {
        if (crypt != null) {
            return crypt;
        }
        synchronized (Crypt.class) {
            if (crypt == null) {
                crypt = new Crypt();
            }
        }
        return crypt;
    }


    public String getBase64Encoded(Key key){
        byte[] encoded = key.getEncoded();
        String b64Key = Base64.getEncoder().encodeToString(encoded);
        return b64Key;
    }

    public PublicKey getPublicKey(String algo,String base64PublicKey){
        byte [] binCpk = Base64.getDecoder().decode(base64PublicKey);
        X509EncodedKeySpec pkSpec = new X509EncodedKeySpec(binCpk);

        try {
            KeyFactory keyFactory = KeyFactory.getInstance(algo);
            PublicKey pKey = keyFactory.generatePublic(pkSpec);
            return pKey;
        }catch (NoSuchAlgorithmException | InvalidKeySpecException ex){
            throw new RuntimeException(ex);
        }
    }
    public PrivateKey getPrivateKey(String algo,String base64PrivateKey){
        byte [] binCpk = Base64.getDecoder().decode(base64PrivateKey);
        PKCS8EncodedKeySpec pkSpec = new PKCS8EncodedKeySpec(binCpk);

        try {
            KeyFactory keyFactory = KeyFactory.getInstance(algo);
            PrivateKey pKey = keyFactory.generatePrivate(pkSpec);
            return pKey;
        }catch (NoSuchAlgorithmException | InvalidKeySpecException ex){
            throw new RuntimeException(ex);
        }
    }

    public KeyPair generateKeyPair(String algo,int strength ) {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(algo);
            keyPairGenerator.initialize(strength);
            return keyPairGenerator.generateKeyPair();
        }catch (NoSuchAlgorithmException ex){
            throw new RuntimeException(ex);
        }
    }


    public static final String KEY_ALGO = "RSA";
    public static final String SIGNATURE_ALGO = "SHA256withRSA";
    /*
    public KeyPair generateKeyPair(){
        return generateKeyPair(KEY_ALGO);
    }
    public PublicKey getPublicKey(String base64PublicKey){
        return getPublicKey(KEY_ALGO,base64PublicKey);
    }
    public PrivateKey getPrivateKey(String base64PrivateKey){
        return getPrivateKey(KEY_ALGO,base64PrivateKey);
    }
    */

    public String generateSignature(String payload, String signatureAlgoritm , PrivateKey privateKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature signature = Signature.getInstance(signatureAlgoritm); //
        signature.initSign(privateKey);
        signature.update(payload.getBytes());
        return Base64.getEncoder().encodeToString(signature.sign());
    }
    public boolean verifySignature(String payload, String signature, String signatureAlgorithm ,PublicKey pKey){
        byte [] data = payload.getBytes();
        byte [] signatureBytes = Base64.getDecoder().decode(signature);
        try {
            Signature s = Signature.getInstance(signatureAlgorithm);
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
                Cipher cipher = Cipher.getInstance(algorithm);
                ThreadLocal tl = new ThreadLocal<>();
                tl.set(cipher);
                return tl;
            }catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    };
    public String encrypt(String decrypted, String algorithm, String publicKey){
        Cipher cipher = cipherCache.get(algorithm).get();
        try {
            cipher.init(Cipher.ENCRYPT_MODE,getPublicKey(algorithm,publicKey));
            byte[] encrypted = cipher.doFinal(decrypted.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public String decrypt(String encrypted, String algorithm, String privateKey){
        try {
            Cipher cipher = cipherCache.get(algorithm).get();
            cipher.init(Cipher.DECRYPT_MODE,getPrivateKey(algorithm,privateKey));
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encrypted.getBytes(StandardCharsets.UTF_8)));
            return new String(decrypted);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }


}
