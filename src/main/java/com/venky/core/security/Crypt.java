package com.venky.core.security;

import com.venky.cache.Cache;
import com.venky.core.util.ObjectUtil;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.params.Ed448PrivateKeyParameters;
import org.bouncycastle.crypto.params.X25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.X25519PublicKeyParameters;
import org.bouncycastle.crypto.params.X448PrivateKeyParameters;
import org.bouncycastle.crypto.util.AlgorithmIdentifierFactory;
import org.bouncycastle.internal.asn1.edec.EdECObjectIdentifiers;
import org.bouncycastle.jcajce.provider.asymmetric.edec.BCEdDSAPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.edec.BCEdDSAPublicKey;
import org.bouncycastle.jcajce.provider.asymmetric.edec.BCXDHPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.edec.BCXDHPublicKey;
import org.bouncycastle.jcajce.spec.EdDSAParameterSpec;
import org.bouncycastle.jcajce.spec.OpenSSHPrivateKeySpec;
import org.bouncycastle.jcajce.spec.OpenSSHPublicKeySpec;
import org.bouncycastle.jcajce.spec.XDHParameterSpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmConstraints;
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
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

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
            cache = new Cache<>() {
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
        return getBase64Encoded(key,true);
    }
    private String getBase64EncodedBCObject(Key key, String innerField){
        try {
            Field f = getField(key.getClass(),innerField);
            Object o = f.get(key);
            Method m = o.getClass().getMethod("getEncoded");
            return toBase64((byte[]) m.invoke(o));
        }catch (Exception ex){
            return null;
        }
    }
    public String getBase64Encoded(Key key , boolean asPem){
        String s = null;
        if (!asPem) {
            if (key instanceof BCEdDSAPublicKey) {
                s =  getBase64EncodedBCObject(key,"eddsaPublicKey");
            }
            if (key instanceof BCXDHPublicKey){
                s = getBase64EncodedBCObject(key,"xdhPublicKey");
            }
            if (key instanceof BCEdDSAPrivateKey){
                s = getBase64EncodedBCObject(key, "eddsaPrivateKey");
            }
            if (key instanceof BCXDHPrivateKey){
                s = getBase64EncodedBCObject(key, "xdhPrivateKey");
            }
        }
        if (s == null) {
            s = toBase64(key.getEncoded());
        }
        return s;
    }
    private Field getField(Class<?> clazz, String fieldName){
        Field field = null ;
        while (field == null && clazz != Object.class){
            try {
                field = clazz.getDeclaredField(fieldName);
            }catch (Exception ex){
                clazz = clazz.getSuperclass();
            }
        }
        if (field != null) {
            field.setAccessible(true);
        }
        return field;
        
    }
    
    public SecretKey getSecretKey(String algo,String base64Key){
        return new SecretKeySpec(Base64.getDecoder().decode(base64Key),algo);
    }

    public PublicKey getPublicKey(String algo,String base64PublicKey){
        byte [] binCpk = Base64.getDecoder().decode(base64PublicKey);
        PublicKey key =  getPublicKey(algo,binCpk);
        if (key == null){
            key = getAltPublicKey(algo,binCpk);
        }
        return key;
    }
    public List<KeySpec> getPublicSpecs(byte[] binCpk){
        List<KeySpec> specs = new ArrayList<>();
        specs.add(new X509EncodedKeySpec(binCpk));
        try {
            specs.add(new OpenSSHPublicKeySpec(binCpk));
        }catch (Exception ex){
            //
        }
        return specs;
    }
    public PublicKey getPublicKey(String algo, byte[] binCpk){
        PublicKey key = null;
        for (KeySpec pkSpec : getPublicSpecs(binCpk)){
            try {
                KeyFactory keyFactory = KeyFactory.getInstance(algo,provider);
                key =  keyFactory.generatePublic(pkSpec);
                if (key != null){
                    break;
                }
            }catch (Exception ex){
                //
            }
        }
        return key;
    }
    
    private PublicKey getAltPublicKey(String algo, byte[] binCpk) {
        try {
            switch (algo) {
                case EdDSAParameterSpec.Ed25519 -> {
                    byte[] jceBytes = new SubjectPublicKeyInfo(new AlgorithmIdentifier(EdECObjectIdentifiers.id_Ed25519), binCpk).getEncoded();
                    return getPublicKey(algo,jceBytes);
                }
                case EdDSAParameterSpec.Ed448 -> {
                    byte[] jceBytes = new SubjectPublicKeyInfo(new AlgorithmIdentifier(EdECObjectIdentifiers.id_Ed448), binCpk).getEncoded();
                    return getPublicKey(algo,jceBytes);
                }
                case XDHParameterSpec.X25519 -> {
                    byte[] jceBytes = new SubjectPublicKeyInfo(new AlgorithmIdentifier(EdECObjectIdentifiers.id_X25519), binCpk).getEncoded();
                    return getPublicKey(algo,jceBytes);
                }
                case XDHParameterSpec.X448 -> {
                    byte[] jceBytes = new SubjectPublicKeyInfo(new AlgorithmIdentifier(EdECObjectIdentifiers.id_X448), binCpk).getEncoded();
                    return getPublicKey(algo,jceBytes);
                }
                default -> {
                    return null;
                }
            }
        }catch (Exception ex){
            return null;
        }
    }
    
    public PrivateKey getPrivateKey(String algo,String base64PrivateKey){
        byte [] binCpk = Base64.getDecoder().decode(base64PrivateKey);
        return getPrivateKey(algo,binCpk);
    }
    
    public List<KeySpec> getPrivateSpecs(byte[] binCpk){
        List<KeySpec> specs = new ArrayList<>();
        specs.add(new PKCS8EncodedKeySpec(binCpk));
        try {
            specs.add(new OpenSSHPrivateKeySpec(binCpk));
        }catch (Exception ex){
            //
        }
        return specs;
    }
    
    public PrivateKey getPrivateKey(String algo, byte[] binCpk) {
        PrivateKey key = null;
        for (KeySpec pkSpec : getPrivateSpecs(binCpk)){
            try {
                KeyFactory keyFactory = KeyFactory.getInstance(algo,provider);
                key = keyFactory.generatePrivate(pkSpec);
                if (key != null){
                    break;
                }
            }catch (NoSuchAlgorithmException | NoSuchProviderException |InvalidKeySpecException ex){
                //
            }
        }
        if (key == null){
            key  = getAltPrivateKey(algo,binCpk);
        }
        
        return key;
    }
    private PrivateKey getAltPrivateKey(String algo, byte[] binCpk) {
        PrivateKey privateKey = null;
        byte[] jceBytes = null;
        try {
            switch (algo) {
                case EdDSAParameterSpec.Ed25519 -> {
                    //Ed25519PrivateKeyParameters privateKeyParameters = new Ed25519PrivateKeyParameters(binCpk);
                    jceBytes = new PrivateKeyInfo(new AlgorithmIdentifier(EdECObjectIdentifiers.id_Ed25519),
                            new DEROctetString(binCpk)).getEncoded();
                }
                case EdDSAParameterSpec.Ed448 -> {
                    jceBytes = new PrivateKeyInfo(new AlgorithmIdentifier(EdECObjectIdentifiers.id_Ed448),
                            new DEROctetString(binCpk)).getEncoded();
                }
                case XDHParameterSpec.X25519 -> {
                    jceBytes = new PrivateKeyInfo(new AlgorithmIdentifier(EdECObjectIdentifiers.id_X25519),
                            new DEROctetString(binCpk)).getEncoded();
                }
                case XDHParameterSpec.X448 -> {
                    jceBytes = new PrivateKeyInfo(new AlgorithmIdentifier(EdECObjectIdentifiers.id_X448),
                            new DEROctetString(binCpk)).getEncoded();
                }
                default -> {
                    return null;
                }
            }
        }catch (Exception ex){
            //
        }
        if (jceBytes != null) {
            privateKey = getPrivateKey(algo, jceBytes);
        }
        
        return privateKey;
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
        return generateSignature(payload.getBytes(StandardCharsets.UTF_8),signatureAlgorithm,privateKey);
    }
    
    public String generateSignature(byte[] payload, String signatureAlgorithm , PrivateKey privateKey)  {
        try {
            Signature signature = Signature.getInstance(signatureAlgorithm, provider); //
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
            Signature s = Signature.getInstance(signatureAlgorithm,provider);
            s.initVerify(pKey);
            s.update(data);
            return s.verify(signatureBytes);
        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
    
    
    Cache<String, Cipher> cipherCache = new Cache<>() {
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
        for (byte aByte : bytes) {
            String hex = Integer.toHexString(aByte);
            if (hex.length() == 1) {
                hex = "0" + hex;
            }
            hex = hex.substring(hex.length() - 2);
            builder.append(hex);
        }
        return builder.toString();
    }

}
