package com.venky.cache;

import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.Channel;
import java.nio.channels.SeekableByteChannel;

public class ChannelOutputStream extends OutputStream {
    RandomAccessFile randomAccessFile = null;
    public ChannelOutputStream(RandomAccessFile raf) {
        this.randomAccessFile = raf;
    }

    @Override
    public void write(int b) throws IOException {
        randomAccessFile.write(b);
    }
    public void write(byte b[], int off, int len) throws IOException {
        randomAccessFile.write(b,off,len);
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
    public void close() throws IOException {
        randomAccessFile.close();
    }

}
