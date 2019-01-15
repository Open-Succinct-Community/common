package com.venky.core.util.pkg;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarIntrospector extends PackageIntrospector{
	
	private final File pkgFile ;
	public JarIntrospector(File f){
		this.pkgFile = f; 
	}

	@Override
	public List<String> getFiles(String path, Predicate<String> filter) {
		List<String> classes = new ArrayList<String>();
		try {
	        JarFile jf = new JarFile(pkgFile); 
	        Enumeration<JarEntry> jes =jf.entries();
	        List<PackageIntrospector> children = new ArrayList<PackageIntrospector>();
	        while (jes.hasMoreElements()){
	            JarEntry je = jes.nextElement();
	            String jeName = (File.separatorChar != '/') ? je.getName().replace(File.separatorChar, '/') : je.getName() ;
	            
	            if (jeName.startsWith(path)){
	            	if (filter.test(jeName)){
	            		classes.add(jeName);
					}
	            }else if (jeName.endsWith(".dex")) {
	            	children.add(new DexIntrospector(pkgFile));
	            }
	        }
	        jf.close();
	        for (PackageIntrospector pi :children){
		        classes.addAll(pi.getClasses(path));
	        }
		}catch (Exception ex){
			throw new RuntimeException(ex);
		}
		return classes;
	}
}
