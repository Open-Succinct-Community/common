package com.venky.cache;

import java.io.*;
import java.nio.channels.FileLock;

public class Store {
    RandomAccessFile randomAccessFile ;
    File file ;
    ChannelInputStream channelInputStream;
    ChannelOutputStream channelOutputStream;
    public Store(File file, String mode) throws FileNotFoundException{
        this.file = file;
        this.randomAccessFile = new RandomAccessFile(file,mode);
        this.channelInputStream = new ChannelInputStream(this);
        this.channelOutputStream = new ChannelOutputStream(this);
    }

    public ChannelInputStream getInputStream(){
        return channelInputStream;
    }

    public ChannelOutputStream getOutputStream(){
        return channelOutputStream;
    }

    public long size() throws IOException {
        return randomAccessFile.length();
    }

    public long position() throws IOException {
        return randomAccessFile.getFilePointer();
    }

    public void position(long position) throws IOException {
        randomAccessFile.seek(position);
    }

    public void truncate(long size) throws IOException {
        randomAccessFile.setLength(size);
    }

    public int read() throws IOException {
        return randomAccessFile.read();
    }
    public int read(byte b[], int off, int len) throws IOException {
        return randomAccessFile.read(b,off,len);
    }

    public void write(int b) throws IOException {
        randomAccessFile.write(b);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        randomAccessFile.write(b,off,len);
    }

    public void close() throws IOException {
        randomAccessFile.close();
    }

    public void delete() throws  IOException{
        close();
        file.delete();
    }

    public FileLock lock() throws IOException {
        FileLock lock = randomAccessFile.getChannel().lock();
        return lock;
    }
}
