package com.venky.core.collections;

import com.venky.cache.Cache;

public class LowerCaseStringCache extends Cache<String, String>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6371406286614896615L;
	private LowerCaseStringCache(){
		super();
	}
	
	private static LowerCaseStringCache instance =  new LowerCaseStringCache();
	public static LowerCaseStringCache instance(){
		return instance;
	}
	

	@Override
	protected String getValue(String k) {
		return String.valueOf(k).toLowerCase();
	}

}
