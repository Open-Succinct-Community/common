package com.venky.core.io;

import java.io.IOException;
import java.io.OutputStream;


public class SeekableByteArrayOutputStream extends OutputStream{

	private byte[] buf ;
	private int pos;
	private int length;
	private int minSize;
	public SeekableByteArrayOutputStream(){
		this(256);
	}
	private SeekableByteArrayOutputStream(int minSize){
		this.minSize = minSize;
		this.buf = new byte[minSize];
		this.pos = 0;
		this.length = 0;
	}
	
	private void ensure(long size){
		if (buf.length < size){
			grow();
		}
	}
	private void grow(){
		int newSize = buf.length + minSize;
		if (newSize < 0){
			newSize = Integer.MAX_VALUE;
			if (newSize <= buf.length){
				throw new OutOfMemoryError();
			}
		}
		
        buf = copyOf(buf, newSize);
	}
	/** Copied from Arrays in 1.7 */ 
    public static byte[] copyOf(byte[] original, int newLength) {
        byte[] copy = new byte[newLength];
        System.arraycopy(original, 0, copy, 0,
                         Math.min(original.length, newLength));
        return copy;
    }

	public void seek(long pos) {
		this.pos = (int)pos;
	}

	@Override
	public void write(int b) throws IOException {
		ensure(Math.min(length,pos) + 1);
		buf[pos++] = (byte)b;
		length = Math.max(length,pos);
	}

	public byte[] toByteArray() {
		return copyOf(buf, length);
	}

	public long size() {
		return length;
	}
	
	
}
