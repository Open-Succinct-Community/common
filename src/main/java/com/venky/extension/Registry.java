package com.venky.extension;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import com.venky.core.collections.SequenceSet;


public class Registry {
	private Registry(){
		
	}
	private static Registry instance = new Registry();
	public static Registry instance(){
		return instance;
	}
	private HashMap<String, List<Extension>> extensionsMap = new HashMap<String, List<Extension>>();
	public void registerExtension(String name,Extension extension){
		Logger.getLogger(getClass().getName()).finest("Registering extension "  + name + " with " +extension.getClass().getName());
		List<Extension> extensions = getExtensions(name);
		extensions.add(extension);
	}
	
	public List<Extension> getExtensions(String extensionPoint){
		List<Extension> extensions = extensionsMap.get(extensionPoint);
		if (extensions == null){
			extensions = new SequenceSet<Extension>();
			extensionsMap.put(extensionPoint,extensions);
		}
		return extensions;
	}
	
	public void deregisterExtension(String extensionPoint, Extension extension) {
		List<Extension> extensions = getExtensions(extensionPoint);
		extensions.remove(extension);
	}
	public boolean hasExtensions(String extensionPoint){
		return !getExtensions(extensionPoint).isEmpty();
	}
	
	public void callExtensions(String pointName, Object... context){
		Logger.getLogger(getClass().getName()).finest("Calling extensions "  + pointName);
		for (Extension extn : getExtensions(pointName)){
			extn.invoke(context);
		}
	}
	
	public void clearExtensions(){
		extensionsMap.clear();
	}
}
