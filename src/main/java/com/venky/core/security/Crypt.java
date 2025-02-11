package com.venky.core.security;

import com.venky.cache.Cache;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.DSAPublicKeySpec;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.EdECPoint;
import java.security.spec.EdECPrivateKeySpec;
import java.security.spec.EdECPublicKeySpec;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.NamedParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.security.spec.XECPrivateKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class Crypt {
    private Crypt(){
    }
    static ThreadLocal<Crypt> cryptInstance = new ThreadLocal<>();
    public static Crypt getInstance() {
        Crypt crypt =  cryptInstance.get();
        if (crypt == null){
            crypt = new Crypt();
            cryptInstance.set(crypt);
        }
        return cryptInstance.get();
    }
   
    public String getBase64Encoded(Key key){
        byte[] encoded = key.getEncoded();
        return Base64.getEncoder().encodeToString(encoded);
    }
    public SecretKey getSecretKey(String algo,String base64Key){
        return new SecretKeySpec(Base64.getDecoder().decode(base64Key),algo);
    }

    public PublicKey getPublicKey(String algo,String base64PublicKey){
        byte [] binCpk = Base64.getDecoder().decode(base64PublicKey);
        return getPublicKey(algo,binCpk);
    }

    public PublicKey getPublicKey(String algo,byte[] binCpk){
        PublicKey key = null;
        
        for (KeySpec spec : getPublicKeySpecs(algo,binCpk)){
            key = getPublicKey(algo,spec);
            if (key != null){
                break;
            }
        }
        return key;
    }
    private List<KeySpec> getPublicKeySpecs(String algo, byte[] binCpk){
        List<KeySpec> specs = new ArrayList<>();
        specs.add(new X509EncodedKeySpec(binCpk));
        specs.add(new EdECPublicKeySpec(NamedParameterSpec.ED25519,byteArrayToEdPoint(binCpk)));
        
        return specs;
    }
    private static EdECPoint byteArrayToEdPoint(byte[] arr){
        byte msb = arr[arr.length - 1];
        boolean xOdd = (msb & 0x80) != 0;
        arr[arr.length - 1] &= (byte) 0x7F;
        reverse(arr);
        BigInteger y = new BigInteger(1, arr);
        return new EdECPoint(xOdd, y);
    }
    private static void reverse(byte [] arr){
        int i = 0;
        int j = arr.length - 1;
        
        while(i < j)
        {
            swap(arr, i, j);
            i++;
            j--;
        }
    }
    
    private static void swap(byte[] arr, int i, int j){
        byte tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
    }
    
    public PublicKey getPublicKey(String algo,  KeySpec spec){
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(algo);
            return keyFactory.generatePublic(spec);
        }catch (NoSuchAlgorithmException | InvalidKeySpecException ex){
            return null;
        }
    }
    private List<KeySpec> getPrivateKeySpecs(String algo, byte[] binCpk){
        List<KeySpec> specs = new ArrayList<>();
        specs.add(new PKCS8EncodedKeySpec(binCpk));
        if (algo.startsWith("Ed")) {
            specs.add(new EdECPrivateKeySpec(new NamedParameterSpec(algo), binCpk));
        }
        if (algo.startsWith("X")){
            specs.add(new XECPrivateKeySpec(new NamedParameterSpec(algo), binCpk));
        }
        try {
            specs.add(new DESKeySpec(binCpk));
        }catch (Exception ex){
            //
        }
        specs.add(new EncodedKeySpec(binCpk) {
            @Override
            public String getFormat() {
                return "RAW";
            }
        });
        return specs;
    }
    
    public PrivateKey getPrivateKey(String algo,String base64PrivateKey){
        byte [] binCpk = Base64.getDecoder().decode(base64PrivateKey);
        return getPrivateKey(algo,binCpk);
    }
    public PrivateKey getPrivateKey(String algo,byte[] binCpk){
        PrivateKey key = null;
        for (KeySpec spec : getPrivateKeySpecs(algo,binCpk)){
            key = getPrivateKey(algo,spec);
            if (key != null){
                break;
            }
        }
        return key;
    }
    
    public PrivateKey getPrivateKey(String algo, KeySpec spec){
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(algo);
            return keyFactory.generatePrivate(spec);
        }catch (NoSuchAlgorithmException | InvalidKeySpecException ex){
            return null;
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

    public String generateSignature(String payload, String signatureAlgorithm , PrivateKey privateKey)  {
        return generateSignature(payload.getBytes(StandardCharsets.UTF_8),signatureAlgorithm,privateKey);
    }
    
    public String generateSignature(byte[] payload, String signatureAlgorithm , PrivateKey privateKey)  {
        try {
            Signature signature = Signature.getInstance(signatureAlgorithm); //
            signature.initSign(privateKey);
            signature.update(payload);
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
            Signature s = Signature.getInstance(signatureAlgorithm);
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
                return Cipher.getInstance(algorithm);
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
            MessageDigest digest = MessageDigest.getInstance(algorithm);
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
