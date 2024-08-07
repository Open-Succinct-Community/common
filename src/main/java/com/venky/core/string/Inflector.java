/*
Copyright 2009-2010 Igor Polevoy 

Licensed under the Apache License, Version 2.0 (the "License"); 
you may not use this file except in compliance with the License. 
You may obtain a copy of the License at 

http://www.apache.org/licenses/LICENSE-2.0 

Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS, 
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
See the License for the specific language governing permissions and 
limitations under the License. 
*/

/* This file is a derived from work of Igor Polevoy on active jdbc project */


package com.venky.core.string;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.venky.core.collections.IgnoreCaseList;
import com.venky.core.collections.LowerCaseStringCache;
import com.venky.core.collections.UpperCaseStringCache;

public class Inflector {

    private static List<String[]> singulars, plurals, irregulars;
    private static List<String> uncountables;

    static {
        singulars = new ArrayList<String[]>();
        plurals = new ArrayList<String[]>();
        irregulars = new ArrayList<String[]>();
        uncountables = new IgnoreCaseList();

        addPlural("$", "s");
        addPlural("s$", "s");
        addPlural("(ax|test)is$", "$1es");
        addPlural("(octop|vir)us$", "$1i");
        addPlural("(alias|status)$", "$1es");
        addPlural("(bu)s$", "$1ses");
        addPlural("(buffal|(t|m)at)o$", "$1oes");
        addPlural("([ti])um$", "$1a");
        addPlural("sis$", "ses");
        addPlural("(?:([^f])fe|([lr])f)$", "$1$2ves");
        addPlural("(hive)$", "$1s");
        addPlural("([^aeiouy]|qu)y$", "$1ies");
        addPlural("(x|ch|ss|sh)$", "$1es"); 
        addPlural("(matr|vert|ind)(?:ix|ex)$", "$1ices");
        addPlural("([m|l])ouse$", "$1ice");
        addPlural("^(ox)$", "$1en");
        addPlural("(quiz)$", "$1zes");


        addSingular("s$", "");
        addSingular("(n)ews$", "$1ews");
        addSingular("([ti])a$", "$1um");
        addSingular("((a)naly|(b)a|(d)iagno|(p)arenthe|(p)rogno|(s)ynop|(t)he)ses$", "$1sis");
        addSingular("(^analy)ses$", "$1sis");
        addSingular("([^f])ves$", "$1fe");
        addSingular("(hive)s$", "$1");
        addSingular("(tive)s$", "$1");
        addSingular("([lr])ves$", "$1f");
        addSingular("([^aeiouy]|qu)ies$", "$1y");
        addSingular("(s)eries$", "$1eries");
        addSingular("(m)ovies$", "$1ovie");
        addSingular("(x|ch|ss|sh)es$", "$1");
        addSingular("([m|l])ice$", "$1ouse");
        addSingular("(bus)es$", "$1");
        addSingular("(o)es$", "$1");
        addSingular("(shoe)s$", "$1");
        addSingular("(cris|ax|test)es$", "$1is");
        addSingular("(octop|vir)i$", "$1us");
        addSingular("(alias|status)es$", "$1");
        addSingular("^(ox)en", "$1");
        addSingular("(vert|ind)ices$", "$1ex");
        addSingular("(matr)ices$", "$1ix");
        addSingular("(quiz)zes$", "$1");
        addSingular("(database)s$", "$1");

        addIrregular("person", "people");
        addIrregular("man", "men");
        addIrregular("child", "children");
        addIrregular("sex", "sexes");
        addIrregular("move", "moves");

        uncountables.addAll(Arrays.asList("equipment", "information", "rice", "money", "species", "series", "fish", "sheep"));
    }

    public static void addPlural(String rule, String replacement){
        plurals.add(0, new String[]{rule, replacement});
    }

    public static void addSingular(String rule, String replacement){
        singulars.add(0, new String[]{rule, replacement});
    }

    public static void addIrregular(String rule, String replacement){
        irregulars.add(new String[]{rule, replacement});
    }




    /**
     * Replaces a found pattern in a word and returns a transformed word.
     * @param word
     * @param rule
     * @param replacement
     * @return Replaces a found pattern in a word and returns a transformed word. Null is pattern does not match.
     */
    public static String gsub(String word, String rule, String replacement) {
        Pattern pattern = Pattern.compile(rule, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(word);
        return matcher.find() ? matcher.replaceFirst(replacement) : null;
    }
    
    public static String pluralize(String word) {
        if(uncountables.contains(word)) return word;

        for (String[] irregular : irregulars) {
            if (irregular[0].equalsIgnoreCase(word)) {
                return irregular[1];
            }
        }
        
        for (String[] pair: plurals) {
            String plural = gsub(word, pair[0], pair[1]);
            if (plural != null)
                return plural;
        }
        
        return word;
    }


     public static String singularize(String word) {

        if(uncountables.contains(word)) return word;

        for (String[] irregular : irregulars) {
            if (irregular[1].equalsIgnoreCase(word)) {
                return irregular[0];
            }
        }

        for (String[] pair: singulars) {
            String singular = gsub(word, pair[0], pair[1]);
            if (singular != null)
                return singular;
        }

        return word;
    }
    public static String underscore(String camel) {
        return underscore(camel,"_");
    }
    public static String kebab(String camel){
        return underscore(camel,"-");
    }
    private static String underscore(String camel, String underscore_character) {

        List<Integer> upper = new ArrayList<Integer>();
        byte[] bytes = camel.getBytes();
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            if (b < 97 || b > 122) {
                upper.add(i);
            }
        }

        StringBuilder b = new StringBuilder(camel);
        for (int i = upper.size() - 1; i >= 0; i--) {
            Integer index = upper.get(i);
            if (index != 0)
                b.insert(index, underscore_character);
        }

        return UpperCaseStringCache.instance().get(b.toString());
    }
    public static String camelize(String someString , boolean capitalizeFirstCharacter) {
        char[] chars = someString.toCharArray();
        StringBuilder builder = new StringBuilder();
        int pos = 0;
        boolean capitalize = capitalizeFirstCharacter;

        for (;pos < chars.length; pos ++){
            char curr = chars[pos];
            if (curr == '_'){
                // skip
                capitalize = true;
            }else if (capitalize){
                builder.append(Character.toUpperCase(curr));
                capitalize = false;
            }else {
                builder.append(Character.toLowerCase(curr));
            }
        }
        return builder.toString();

    }



    /**
     * Generates a camel case version of a phrase from underscore.
     *
     * @param underscore underscore version of a word to converted to camel case.
     * @return camel case version of underscore.
     */
    public static String camelize(String underscore){
        return camelize(underscore, true);
    }



}
