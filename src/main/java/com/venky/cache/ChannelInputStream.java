package com.venky.cache;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

public class ChannelInputStream extends InputStream {
    Store store = null;
    public ChannelInputStream(Store store) {
        this.store = store;
    }

    @Override
    public int read() throws IOException {
        return store.read();
    }
    @Override
    public int read(byte b[], int off, int len) throws IOException {
        return store.read(b,off,len);
    }
    @Override
    public int available() throws IOException {
        long available = store.size() - store.position();
        return (int)(available > 0 ? available : 0);
    }
}
