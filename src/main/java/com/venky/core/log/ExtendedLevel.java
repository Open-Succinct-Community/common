package com.venky.core.log;

import java.util.logging.Level;

public class ExtendedLevel extends Level{

	public static final Level TIMER = new ExtendedLevel("TIMER", Level.FINEST.intValue() - 100);
	
	protected ExtendedLevel(String name, int value) {
		super(name, value);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 340834407017154841L;

}
