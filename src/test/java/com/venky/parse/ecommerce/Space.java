package com.venky.parse.ecommerce;

import com.venky.parse.character.Include;
import com.venky.parse.composite.OneOrMore;

public class Space extends Include {
    public static final char [] SPACES = new char[] { ' ','\t','\n','\f' };
    public Space(){
        super(SPACES);
    }
    
    public static class Spaces extends OneOrMore {
        public Spaces() {
            super(new Space());
        }
    }
}
