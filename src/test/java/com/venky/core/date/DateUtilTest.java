package com.venky.core.date;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.junit.Test;

public class DateUtilTest {

	@Test
	public void test() throws ParseException {
		new SimpleDateFormat(DateUtils.ISO_DATE_FORMAT_STR).parse("2016-06-24");
	}

}
