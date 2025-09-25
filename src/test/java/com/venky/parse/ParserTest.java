package com.venky.parse;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.venky.parse.character.Include;
import com.venky.parse.composite.Any;
import com.venky.parse.composite.Sequence;
import com.venky.parse.composite.ZeroOrMore;
import com.venky.parse.ecommerce.Product;
import com.venky.parse.ecommerce.Word;
import com.venky.parse.string.RegExRule;
import org.junit.Assert;
import org.junit.Test;

import java.util.regex.Pattern;

public class ParserTest {
    
    @Test
    public void test(){
        Rule rule  =new RegExRule("A.*Z.*?");
        if (rule.match("A2ZABC")){
            Assert.assertEquals(rule.getMatch().getText(),"A2Z");
        }
        
    }
   
}
