package com.venky.parse.character;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


public class Exclude extends AbstractSingleCharacterRule{

	Set<Character> exclude = null;
	
	public Exclude(char... exclude){
		this.exclude = new HashSet<>();
		for (int  i = 0 ; i < exclude.length ; i++){
			this.exclude.add(exclude[i]);
		}
	}
	public Exclude(Collection<Character> exclude){
		this.exclude = new HashSet<>(exclude);
	}
	@Override
	protected boolean match(char c) {
		return !this.exclude.contains(c);
	}

}
