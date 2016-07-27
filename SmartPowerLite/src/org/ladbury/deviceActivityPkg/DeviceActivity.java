package org.ladbury.deviceActivityPkg;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;


import org.ladbury.deviceActivityPkg.DeviceActivity;
import org.ladbury.persistentData.Persistable;
import org.ladbury.smartpowerPkg.Timestamped;

import java.util.Comparator;

public class DeviceActivity implements 	Serializable,
										Comparator<DeviceActivity>,
										Comparable<DeviceActivity>,
										Persistable<DeviceActivity>,
										Timestamped<DeviceActivity>{
	
	private static final long serialVersionUID = -8378607274521362293L;	
	private long 		activityId;
	private Timestamp 	startTimestamp;
	private Timestamp 	endTimeStamp;
	private int			consumption;
	private String 		device;
	
	//
	// Constructors
	//
	public DeviceActivity() {
		super();
		this.device = null;
		this.startTimestamp = null;
		this.endTimeStamp = null;
		this.consumption = -1;
	}

	public DeviceActivity(Timestamp 	activityStart, 
							Timestamp 	activityEnd,
							int			consumption,
							String 		device) {
		this();
		this.startTimestamp = activityStart;
		this.endTimeStamp = activityEnd;
		this.consumption = consumption;
		this.device = device;
	}

	//
	// Simple Getters & Setters
	//
	public void setActivityId(long activityId) {
		this.activityId = activityId;
	}

	public Timestamp start() {
		return startTimestamp;
	}
	public void setStart(Timestamp activityStart) {
		this.startTimestamp = activityStart;
	}

	public Timestamp end() {
		return endTimeStamp;
	}

	public void setEnd(Timestamp activityStop) {
		this.endTimeStamp = activityStop;
	}

	public String getDevice() {
		return device;
	}
	
	public int getConsumption() {
		return consumption;
	}
	
	//
	// Methods Supporting Entity relationships
	//
/*
	public void setDevice(Device param) {
		//handle the bidirectional link to Device
		if (this.device != null){
			if (param==null){ // remove old reference
				this.device.getDeviceActivities().remove(this);			
			}else{ //change references
				if (this.device.id()!=param.id()){
					this.device.getDeviceActivities().remove(this);
					param.getDeviceActivities().add(this);
				}
			}	
		} else{
			if (param!=null){ //add new reference			
				param.getDeviceActivities().add(this);
			}
		}
		this.device = param;
	}*/
	//
	// additional functions
	//
	
    public String startTimestampString(){
		DateFormat df = new SimpleDateFormat(Timestamped.OUTPUTDATEFORMAT);
		return df.format(startTimestamp);
	}

    public String endTimestampString(){
		DateFormat df = new SimpleDateFormat(Timestamped.OUTPUTDATEFORMAT);
		return df.format(endTimeStamp);
	}
    public String durationString(){
    	long t = Math.abs(endTimeStamp.getTime()-startTimestamp.getTime());
		DateFormat df = new SimpleDateFormat(Timestamped.TIMEFORMAT);
		df.setTimeZone(java.util.TimeZone.getTimeZone("GMT")); // makes sure we don't get an extra hour!
		return df.format(new Timestamp(t)); 
   	
    }
    
    public long getDuration(){
    	return Math.abs(endTimeStamp.getTime()-startTimestamp.getTime());
   	
    }
    //
    //Methods Supporting interfaces
    //
    
	@Override //Persistable
	public long id() {
		return activityId;
		}

	@Override //Persistable
	public String idString(){
		return new String("["+this.id()+"] "+this.name());
	}
	
    @Override //Persistable
    public String name(){
    	return timestampString()+"-"+(device!=null?device:"Unknown");
    }
    
	@Override //Persistable
	public void updateFields(DeviceActivity da) {
/*		this.startTimestamp = da.startTimestamp;
		this.endTimeStamp = da.endTimeStamp;
		setDevice(da.device);
		this.consumption = da.consumption;*/
	}

   @Override //Persistable
    public String toCSV(){
    	return new String(	id()+","+
    						startTimestampString()+","+
    						endTimestampString()+ ","+
    						(device!=null?device:"Unknown"+",")+
    						consumption +"," +
    						durationString());
    }

	@Override //Comparator
	public int compare(DeviceActivity da1, DeviceActivity da2) {
		return da1.startTimestamp.compareTo(da2.startTimestamp);
	}

	@Override //Comparable
	public int compareTo(DeviceActivity o) {
		return compare(this,o);
	}
	
	@Override //Timestamped	
	public Timestamp timestamp() {
		return startTimestamp;
	}
	
	@Override //Timestamped
    public String timestampString(){
		DateFormat df = new SimpleDateFormat(Timestamped.OUTPUTDATEFORMAT);
		return df.format(startTimestamp);
	}

	@Override //Timestamped
	public boolean happenedBetween(Timestamp ts1, Timestamp ts2) {
		return ((timestamp().compareTo(ts1)>=0) && (timestamp().compareTo(ts2)<=0)  );
	}
}
