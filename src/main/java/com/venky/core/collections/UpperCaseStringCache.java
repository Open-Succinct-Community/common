package com.venky.core.collections;

import com.venky.cache.Cache;

public class UpperCaseStringCache extends Cache<String, String>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7183202541501222694L;
	private UpperCaseStringCache(){
		super();
	}
	
	private static UpperCaseStringCache instance =  new UpperCaseStringCache();
	public static UpperCaseStringCache instance(){
		return instance;
	}
	

	@Override
	protected String getValue(String k) {
		return String.valueOf(k).toUpperCase();
	}

}
