package org.ladbury.meterPkg;

import java.sql.Timestamp;

@SuppressWarnings("SpellCheckingInspection")
public interface Timestamped<E> {
	String OUTPUTDATEFORMAT = "dd/MM/yyyy HH:mm:ss";
	String DATE_AND_DAYFORMAT = "EEEE dd/MM/yyyy";
	String FILE_DATE_FORMAT = "yyyy-MM-dd";
	String FILE_DATE_AND_DAY_FORMAT = "yyyy-MM-dd EEEE";
	String NUMERICDATEFORMAT = "yyyy-MM-dd-HH-mm";
	String COMPACTDATEFORMAT = "yyyyMMddHHmm";
	String TIMEFORMAT = "HH:mm:ss";
	int SECOND_IN_MS = 1000;
	int MINUTE_IN_MS = SECOND_IN_MS*60;
	int HOUR_IN_MS = MINUTE_IN_MS*60;
	int DAY_IN_MS = HOUR_IN_MS*24;

	Timestamp timestamp();
	String timestampString();
	boolean happenedBetween(Timestamp ts1, Timestamp ts2);
	
}