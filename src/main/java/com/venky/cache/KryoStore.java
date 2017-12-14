package com.venky.cache;

import java.io.*;
import java.lang.reflect.InvocationHandler;
import java.nio.channels.FileLock;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.CompatibleFieldSerializer;

import de.javakaffee.kryoserializers.JdkProxySerializer;


public  class KryoStore { 
	Store fileStore ;
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
		try {
            fileStore = new Store(store,"rws");
			kryo = createCryo();
			ki = new Input(fileStore.getInputStream());
			ko = new Output(fileStore.getOutputStream());
		} catch (FileNotFoundException e) {
			throw new KryoException(e); //Soften the exception.
		}
		
	}
	public void flush() {
        ko.flush();
    }
	public boolean eof() {
	    try {
            return (fileStore.getInputStream().available() <=0 && ki.available() <= 0);
        }catch (IOException e){
	        if (e instanceof  EOFException){
	            return true;
            }
            throw new RuntimeException(e);
        }
	}
	public long position() {
		try {
			return fileStore.position() - ( ki.limit() - ki.position());
		} catch (IOException e) {
			throw new KryoException(e); //Soften the exception.
		}
	}
	public void position(long offset){
		try {
			fileStore.position(offset);
			ki = new Input(fileStore.getInputStream());
			ko = new Output(fileStore.getOutputStream());
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
			fileStore.truncate(size);
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

	public void close(){
		ki.close();
		ko.close();
        try {
            fileStore.close();
        } catch (IOException e) {
            throw new KryoException(e);
        }
    }
	public void delete() {
		close();
        try {
            fileStore.delete();
        } catch (IOException e) {
            throw new KryoException(e);
        }
    }

	public long size(){
        try {
            return fileStore.size();
        } catch (IOException e) {
            throw new KryoException(e);
        }
    }
    public FileLock lock() {
        try {
            return fileStore.lock();
        } catch (IOException e) {
            throw new KryoException(e);
        }
    }
}
