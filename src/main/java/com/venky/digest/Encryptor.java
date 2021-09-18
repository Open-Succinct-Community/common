package com.venky.digest;

import com.venky.core.security.Crypt;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

public class Encryptor {
	public static String encrypt(String key) {
		return encrypt(key,"SHA");
	}
	public static String encrypt(String key, String algorithm) {
		return Crypt.getInstance().toHex(Crypt.getInstance().digest(algorithm,key));
	}
}
