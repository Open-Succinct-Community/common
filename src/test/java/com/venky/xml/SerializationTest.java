package com.venky.xml;

import java.io.StringWriter;

import org.junit.Test;

public class SerializationTest {

	@Test
	public void test() {
		XMLDocument document = new XMLDocument("Root");
		XMLElement elem = document.createElement("Child");
		document.getDocumentRoot().appendChild(elem);
		
		StringWriter w = new StringWriter();
		XMLSerializationHelper.serialize(document.getDocument(), w);
		XMLSerializationHelper.serialize(elem.getInner(), w);
		System.out.println(w.toString());
	}

	@Test
	public void test2(){
	    XMLDocument document = new XMLDocument("Root");
	    XMLElement element = document.createElement("Child1");
	    element.setAttribute("a","A");

	    System.out.println(element.getAttribute("a"));
	    element = document.createElement("Child2");
        element.getChildElement("a",true).setNodeValue("A");

        System.out.println(element.getChildElement("a").getNodeValue());


    }

}
