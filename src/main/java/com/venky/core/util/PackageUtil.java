/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.venky.core.util;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import com.venky.core.util.pkg.DirectoryIntrospector;
import com.venky.core.util.pkg.JarIntrospector;

/**
 *
 * @author venky
 */
public class PackageUtil {

    public static List<String> getFiles(URL url, String packagePath, Predicate<String> fileFilter) {
        List<String> classes  = new ArrayList<String>();

        if (url.getProtocol().equals("jar")){
            File f = new File(url.getFile().substring("file:".length(), url.getFile().lastIndexOf("!")));
            classes = new JarIntrospector(f).getFiles(packagePath,fileFilter);
        }else if (url.getProtocol().equals("file")) {
            File root = new File(url.getPath());
            classes = new DirectoryIntrospector(root).getFiles(packagePath,fileFilter);
        }
        return classes;
    }
    public static List<String> getClasses(URL url, String packagePath) {
        List<String> classes  = new ArrayList<String>();
        
        if (url.getProtocol().equals("jar")){
            File f = new File(url.getFile().substring("file:".length(), url.getFile().lastIndexOf("!")));
        	classes = new JarIntrospector(f).getClasses(packagePath);
        }else if (url.getProtocol().equals("file")) {
            File root = new File(url.getPath());
            classes = new DirectoryIntrospector(root).getClasses(packagePath);
        }
        return classes;
    }
    
}
