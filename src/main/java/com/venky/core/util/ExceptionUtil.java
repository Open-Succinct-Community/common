/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.venky.core.util;

/**
 *
 * @author venky
 */
public class ExceptionUtil {
    public static Throwable getRootCause(Throwable in){
    	return getEmbeddedException(in,Throwable.class);
    }
    public static Throwable getEmbeddedException(Throwable in, Class<?> instanceOfThisClass){
        Throwable ret = null ;
        Throwable ex = in;
        while (ex != null ){
        	if (instanceOfThisClass.isInstance(ex)){
        		ret = ex;
        	}
        	if (ex instanceof MultiException){
                Throwable tmp = ((MultiException)ex).getContainedException(instanceOfThisClass);
                if (tmp != null){
                    ret = tmp;
                }
            }
        	ex = ex.getCause();
        }
        return ret;
    	
    }
}
