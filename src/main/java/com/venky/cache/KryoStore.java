package com.venky.cache;

import java.io.*;
import java.lang.reflect.InvocationHandler;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CompatibleFieldSerializer;

import de.javakaffee.kryoserializers.JdkProxySerializer;


public  class KryoStore { 
	File fileStore ; 
	ChannelInputStream in;
	ChannelOutputStream out;
	Kryo kryo ;
	Input ki; 
	Output ko;
	private void ensureDir(File directory){
        directory.mkdirs();
        if (!directory.exists()) {
            throw new RuntimeException("Unable to create directory " + directory );
        }else if (!directory.isDirectory()) {
            throw new RuntimeException(directory + " is not a directory!");
        }
    }
	public KryoStore(String storePath) {
		this(new File(storePath));
	}
    public KryoStore(File store) {
        ensureDir(store.getParentFile());
		fileStore = store;
		try {
            RandomAccessFile raf = new RandomAccessFile(fileStore,"rws");
            out = new ChannelOutputStream(raf);
			in = new ChannelInputStream(raf);
			kryo = createCryo();
			ki = new Input(in);
			ko = new Output(out);
		} catch (FileNotFoundException e) {
			throw new KryoException(e); //Soften the exception.
		}
		
	}
	public void flush() {
        ko.flush();
        try {
            out.flush();
        } catch (IOException e) {
            throw new KryoException(e);
        }
    }
	public boolean eof() {
	    try {
            return (in.eof() && ki.available() <= 0);
        }catch (IOException e){
	        if (e instanceof  EOFException){
	            return true;
            }
            throw new RuntimeException(e);
        }
	}
	public long position() {
		try {
			return in.position() - ki.available();
		} catch (IOException e) {
			throw new KryoException(e); //Soften the exception.
		}
	}
	public void position(long offset){
		try {
			in.position(offset);
			ki = new Input(in);
			ko = new Output(out);
		} catch (IOException e) {
			throw new KryoException(e); //Soften the exception.
		}
	}
	@SuppressWarnings("unchecked")
	public <T> T read()  {
		return (T) kryo.readClassAndObject(ki);
	}
	public <T> void write(T object){ 
		kryo.writeClassAndObject(ko, object);
	}
	
	public void truncate(long size) { 
		try {
			out.truncate(size);
		} catch (IOException e) {
			throw new KryoException(e); //Soften the exception.
		}
	}
	
	protected Kryo createCryo(){
		Kryo kryo = new Kryo();
		kryo.setClassLoader(getClass().getClassLoader());
		kryo.setDefaultSerializer(CompatibleFieldSerializer.class);
		kryo.register(InvocationHandler.class,new JdkProxySerializer());
		
		return kryo;
	}

	public void close() { 
		ki.close();
		ko.close();
	}
	public void delete()  {
		close();
		fileStore.delete();
	}

}
