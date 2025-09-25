package com.venky.parse.string;

import com.venky.parse.Rule;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegExRule extends Rule {
    final String regEx;
    final int flags ;
    public RegExRule(String regEx){
        this(regEx,0);
    }
    public RegExRule(String regEx,int flags){
        this.regEx = regEx;
        this.flags = flags;
    }
    
    @Override
    public boolean match(String input, int offset) {
        Pattern pattern = Pattern.compile(regEx,flags);
        Matcher matcher =   pattern.matcher(input.substring(offset));
        boolean ret =  matcher.find();
        
        if (ret){
            //Minimal matching string.
            setMatch(new Element(this,matcher.group()));
        }
        return ret;
    }
}
