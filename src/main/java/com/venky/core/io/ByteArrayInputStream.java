package com.venky.core.io;

import java.io.IOException;
import java.util.Arrays;

public class ByteArrayInputStream extends java.io.ByteArrayInputStream{


	public ByteArrayInputStream(byte[] buf) {
		super(buf);
	}

	@Override
	public void close() throws IOException {
		this.mark = 0;
		reset();
	}
	
	
	
	@Override
	public boolean equals(Object obj) {
		boolean ret;
		
		ret = obj != null && (getClass() == obj.getClass()) ;
		ret = ret && (Arrays.equals(this.buf, ((ByteArrayInputStream)obj).buf));
		return ret;
	}
}
