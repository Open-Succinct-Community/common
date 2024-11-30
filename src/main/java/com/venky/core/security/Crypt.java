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
import java.security.MessageDigest;
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
            Security.insertProviderAt(new BouncyCastleProvider(),1);
        }
    }

    String provider;
    private Crypt(String provider){
        this.provider = provider;
    }
    static ThreadLocal<Cache<String,Crypt>> cachedInstancesByProvider = new ThreadLocal<>();
    public static Crypt getInstance(String provider) {
        Cache<String,Crypt> cache = cachedInstancesByProvider.get();
        if (cache == null){
            cache = new Cache<String, Crypt>() {
                @Override
                protected Crypt getValue(String provider) {
                    return new Crypt(provider);
                }
            };
            cachedInstancesByProvider.set(cache);
        }
        return cache.get(provider);
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
        return verifySignature(payload.getBytes(StandardCharsets.UTF_8),signature,signatureAlgorithm,pKey);
    }
    public boolean verifySignature(byte[] data, String signature, String signatureAlgorithm ,PublicKey pKey){
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
    
    
    Cache<String, Cipher> cipherCache = new Cache<String, Cipher>() {
        @Override
        protected Cipher getValue(String algorithm) {
            try {
                return Cipher.getInstance(algorithm,provider);
            }catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    };
    
    public String encrypt(String decrypted, String algorithm, String publicKey){
        try {
            return encrypt(decrypted,algorithm,getPublicKey(algorithm,publicKey));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    public String encrypt(String decrypted, String algorithm, Key key){
        return encrypt(decrypted.getBytes(StandardCharsets.UTF_8),algorithm,key);
    }
    public String encrypt(byte[] decrypted, String algorithm, Key key){
        Cipher cipher = cipherCache.get(algorithm);
        try {
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encrypted = cipher.doFinal(decrypted);
            return Base64.getEncoder().encodeToString(encrypted);
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

    public String decrypt(String encrypted, String algorithm , Key key){
        return decrypt(encrypted.getBytes(StandardCharsets.UTF_8),algorithm,key);
    }
    public String decrypt(byte[] encrypted, String algorithm , Key key){
        try {
            Cipher cipher = cipherCache.get(algorithm);
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encrypted));
            return new String(decrypted);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    public byte[] digest(String algorithm,String payload){
        return digest(algorithm,payload.getBytes(StandardCharsets.UTF_8));
    }
    public byte[] digest(String algorithm,byte[] data){
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm, provider);
            digest.reset();
            digest.update(data);
            return digest.digest();
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
    public String toBase64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }
    public String toHex(byte[] bytes){
        StringBuilder builder = new StringBuilder();
        for (int i = 0 ; i< bytes.length ; i++ ){
            String hex = Integer.toHexString(bytes[i]);
            if (hex.length() == 1){
                hex = "0"+hex;
            }
            hex = hex.substring(hex.length()-2);
            builder.append(hex);
        }
        return builder.toString();
    }

}
