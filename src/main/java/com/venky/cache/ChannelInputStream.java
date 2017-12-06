package com.venky.cache;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

public class ChannelInputStream extends InputStream {
    RandomAccessFile randomAccessFile = null;
    public ChannelInputStream(RandomAccessFile raf) {
        this.randomAccessFile = raf;
    }



    public void position(long position) throws IOException {
        randomAccessFile.seek(position);
    }
    public long position() throws IOException {
        return randomAccessFile.getFilePointer();
    }

    public void truncate(long size) throws IOException {
        randomAccessFile.setLength(size);
    }

    @Override
    public int read() throws IOException {
        return randomAccessFile.read();
    }

    public boolean eof() throws IOException {
        return randomAccessFile.getFilePointer() < 0 || randomAccessFile.getFilePointer() >= randomAccessFile.length() ;
    }
}
