package com.venky.core.util.pkg;

import java.util.List;

public abstract class PackageIntrospector {

	protected void addClassName(List<String> classes , String fileName){
        if (fileName.endsWith(".class")){
            classes.add(fileName.substring(0,fileName.length() - ".class".length()).replace('/', '.'));
        }
    }

    public abstract List<String> getClasses(String path);

}
