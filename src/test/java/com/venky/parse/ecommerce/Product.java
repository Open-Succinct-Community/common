package com.venky.parse.ecommerce;

import com.venky.parse.composite.OneOrMore;
import com.venky.parse.composite.Sequence;
import com.venky.parse.ecommerce.Space.Spaces;

public class Product extends OneOrMore {
    public Product() {
        super(new Sequence(new Word(),new Spaces()));
    }
}
