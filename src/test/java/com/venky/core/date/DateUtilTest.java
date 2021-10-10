package com.venky.core.date;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Test;

public class DateUtilTest {

	@Test
	public void test() throws ParseException {
		new SimpleDateFormat(DateUtils.ISO_DATE_FORMAT_STR).parse("2016-06-24");
	}
	@Test
	public void testIsoDateTime() {
		DateFormat format = DateUtils.getFormat("yyyy-MM-dd'T'HH:mm:ss");
		format.setTimeZone(TimeZone.getTimeZone("UTC"));
		String s = format.format(new Date());
		System.out.println(s);
		LocalDateTime time = LocalDateTime.parse("2021-06-15T13:40:25.040");

	}
}
