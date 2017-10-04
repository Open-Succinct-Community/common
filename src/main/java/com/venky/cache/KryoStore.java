package com.venky.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
	FileInputStream in; 
	FileOutputStream out; 
	Kryo kryo ;
	Input ki; 
	Output ko; 
	public KryoStore(String storePath) {
		this(new File(storePath));
	}
	public KryoStore(File store) {
		fileStore = store;
		try {
			out = new FileOutputStream(fileStore,true);
			in = new FileInputStream(fileStore);
			kryo = createCryo();
			ki = new Input(in);
			ko = new Output(out);
		} catch (FileNotFoundException e) {
			throw new KryoException(e); //Soften the exception.
		}
		
	}
	public void flush() {
		try {
			ko.flush();
			out.flush();
		} catch (IOException e) {
			throw new KryoException(e); //Soften the exception.
		}
	}
	public boolean eof() { 
		return ki.eof();
	}
	public long getReaderPosition() { 
		try {
			return in.getChannel().position();
		} catch (IOException e) {
			throw new KryoException(e); //Soften the exception.
		}
	}
	public long getWriterPosition() { 
		try {
			return out.getChannel().position();
		} catch (IOException e) {
			throw new KryoException(e); //Soften the exception.
		}
	}

	public void setReaderPosition(long offset){ 
		try {
			in.getChannel().position(offset);
			ki = new Input(in);
		} catch (IOException e) {
			throw new KryoException(e); //Soften the exception.
		}
	}
	public void setWriterPosition(long offset)  { 
		try {
			out.getChannel().position(offset);
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
			out.getChannel().truncate(size);
			in.getChannel().position(Math.min(in.getChannel().position(), size));
			
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
	
	private ReentrantReadWriteLock storeLock = new ReentrantReadWriteLock();
	public Lock lock() { 
		Lock lock =  storeLock.writeLock();
		lock.lock();
		return lock;
	}
}
