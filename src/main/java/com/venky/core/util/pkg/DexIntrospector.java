package com.venky.core.util.pkg;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Logger;

public class DexIntrospector extends PackageIntrospector {
	private final File dexFile;
	public DexIntrospector(File dexFile){
		this.dexFile = dexFile;
	}

	@Override
	public List<String> getFiles(String path, Predicate<String> filter) {
		List<String> files = new ArrayList<String>();
		path = path.replace('/', '.');
		try {
			Object oDexFile = Class.forName("dalvik.system.DexFile").getConstructor(File.class).newInstance(dexFile);
			Method entries = oDexFile.getClass().getMethod("entries");
			@SuppressWarnings("unchecked")
			Enumeration<String> sEntries = (Enumeration<String>) entries.invoke(oDexFile);
			while(sEntries.hasMoreElements()){
				String className = sEntries.nextElement();
				if (className.startsWith(path)){
					if (filter.test(className)){
						files.add(className);
					}
				}
			}
		} catch (Exception ex){
			Logger.getLogger(getClass().getName()).info("Could not parse dex file: " + dexFile.toString() + " due to exception :" + ex.getMessage());
		}
		return files;
	}
}
