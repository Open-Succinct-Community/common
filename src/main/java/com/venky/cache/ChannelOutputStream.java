package com.venky.cache;

import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.Channel;
import java.nio.channels.SeekableByteChannel;

public class ChannelOutputStream extends OutputStream {
    Store store = null;
    public ChannelOutputStream(Store store) {
        this.store = store;
    }

    @Override
    public void write(int b) throws IOException {
        store.write(b);
    }
    @Override
    public void write(byte b[], int off, int len) throws IOException {
        store.write(b,off,len);
    }


}
