package com.venky.parse.character;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


public class Include extends AbstractSingleCharacterRule{

	Set<Character> include = null;
	public Include(char... anyofthese){
		this.include = new HashSet<>();
		for (int i = 0 ; i < anyofthese.length ; i ++){
			this.include.add(anyofthese[i]);
		}
	}
	
	public Include(Collection<Character> anyofthese){
		this.include = new HashSet<>(anyofthese);
	}

	@Override
	protected boolean match(char c) {
		return include.contains(c);
	}

}
