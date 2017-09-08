package org.ladbury.meterPkg;

import java.io.PrintWriter;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.sql.Timestamp;


import org.ladbury.persistentData.Persistable;
import org.ladbury.smartpowerPkg.Timestamped;

//  TimedReading
//  this class provides the basic data for a reading
public class TimedRecord implements 	Serializable,
										Comparator<TimedRecord>,
										Comparable<TimedRecord>,
										Persistable<TimedRecord>,
										Timestamped<TimedRecord>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5233564688108999570L;

	private long		recordId;
	private Timestamp 	timestamp;	//accuracy depends on monitor, Owl recorded readings have a 1 minute granularity, Onzo 1 second
	private int 		value; 		// transition change in consumption reading Watts +ve for an on event -ve for an off event 

	//
    // Constructors
    //
    protected TimedRecord() {
       timestamp = null;
       value = 0;
    }
    protected TimedRecord(Timestamp d) {
        timestamp = d;
        value = 0;
    }
    protected TimedRecord(Timestamp d, int r) {
        timestamp = d;
        value = r;
    }
    public TimedRecord(TimestampedDouble td){
    	value = (int)td.getValue();
    	timestamp = Timestamp.from(td.getTimestamp());
	}
	
	public TimedRecord(String[] dataArray) throws ParseException,ArrayIndexOutOfBoundsException{
		DateFormat df = new SimpleDateFormat(Meter.ONZODATEFORMAT); 
		
		java.util.Date utilDate = df.parse(dataArray[0]);
		timestamp = new Timestamp(utilDate.getTime());
		
		value = new Integer(dataArray[1]);  //watts
	}

	//
	// Additional Methods
	//
	protected void outputCSV(PrintWriter pw){

		DateFormat df = new SimpleDateFormat(Timestamped.OUTPUTDATEFORMAT);
		
		pw.printf("%s,%d", df.format(timestamp), value);
	}

    //
    // accessor methods
    //
	public void setTimestamp(Timestamp ts){
		timestamp = ts;
	}
	public int value(){
		return value;
	}
	protected void setValue(int r){
		value = r;
	}
	//
	// Methods to support interfaces
	//
	@Override //Timestamped
	public Timestamp timestamp(){
		return timestamp;
	}
	@Override //Timestamped
	public String timestampString() {
		DateFormat df = new SimpleDateFormat(Timestamped.OUTPUTDATEFORMAT);
		return df.format(timestamp);
	}
	@Override //Timestamped
	public boolean happenedBetween(Timestamp ts1, Timestamp ts2) {
		return ((timestamp().compareTo(ts1)>=0) && (timestamp().compareTo(ts2)<=0)  );
	}
	@Override //Persistable
	public long id() {
		return recordId;
	}
	@Override //Persistable
	public String name() {
		return timestampString() + "(" + value + ")";
	}
	@Override //Persistable
	public String toCSV() {
		return( this.recordId+","
				+this.timestamp+","
				+this.value);
	}
	@Override //Persistable
	public void updateFields(TimedRecord element) {
	this.timestamp = element.timestamp;
	this.value = element.value;
	}
	@Override //Persistable
	public String idString() {
		return "[" + this.id() + "] " + this.name();
	}
	@Override //Comparator
	public int compareTo(TimedRecord o) {
		return this.timestamp.compareTo(o.timestamp());
	}
	@Override //Comparable
	public int compare(TimedRecord o1, TimedRecord o2) {
		return o1.timestamp.compareTo(o2.timestamp);
	}

}