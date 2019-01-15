package com.venky.core.util.pkg;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class PackageIntrospector {


    public abstract List<String> getFiles(String path, Predicate<String> filter)  ;

    public List<String> getClasses(String path) {
        return getFiles(path, f->f.endsWith(".class")).stream()
                .map(fileName -> fileName.substring(0,fileName.length() - ".class".length()).replace('/', '.'))
                .collect(Collectors.toList());
    }

}
