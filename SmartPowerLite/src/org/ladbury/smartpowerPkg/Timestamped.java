package org.ladbury.smartpowerPkg;

import java.sql.Timestamp;

public interface Timestamped<E> {
	public static final String OUTPUTDATEFORMAT = "dd/MM/yyyy HH:mm:ss"; 
	public static final String DATE_AND_DAYFORMAT = "EEEE dd/MM/yyyy"; 
	public static final String FILE_DATE_FORMAT = "yyyy-MM-dd";
	public static final String FILE_DATE_AND_DAY_FORMAT = "yyyy-MM-dd EEEE";
	public static final String NUMERICDATEFORMAT = "yyyy-MM-dd-HH-mm"; 
	public static final String COMPACTDATEFORMAT = "yyyyMMddHHmm"; 
	public static final String TIMEFORMAT = "HH:mm:ss";
	public static final int SECOND_IN_MS = 1000;
	public static final int MINUTE_IN_MS = SECOND_IN_MS*60;
	public static final int HOUR_IN_MS = MINUTE_IN_MS*60;
	public static final int DAY_IN_MS = HOUR_IN_MS*24;

	public abstract Timestamp timestamp();
	public abstract String timestampString();
	public abstract boolean happenedBetween(Timestamp ts1, Timestamp ts2);
	
}