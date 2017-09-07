package org.ladbury.persistentData;

import java.util.Collections;

import org.ladbury.deviceActivityPkg.DeviceActivity;
import org.ladbury.meterPkg.Meter;
import org.ladbury.meterPkg.Metric;
import org.ladbury.meterPkg.TimedRecord;

/**
 * This is a class to act as the store of the persistent data for
 * the package, and ensure all such data and it's local copy
 * is accessed using a common persistence mechanism such as
 * PersistentList
 * 
 * @author GJWood
 * @version 3.0
 *
 */
@SuppressWarnings("Convert2Diamond")
public class PersistentData {
	public enum EntityType{	UNDEFINED, DEVICE, PARENT_DEVICE, READINGS, EVENTS, //phase1
									ACTIVITY, CATALOGUE, PATTERN, ABODE, ROOM,			//phase1
									MAKE, CATEGORY, WEEKDAYTYPE, TIMEPERIOD, CALENDARPERIOD, HABIT, CLUSTER, //phase2
									METER,METRIC,TIMEDRECORD} //phase 3

	private final PersistentList <DeviceActivity> activity = new PersistentList<DeviceActivity>(Collections.emptyList());
	private final PersistentList <Meter> meters = new PersistentList<Meter>(Collections.emptyList());
	private final PersistentList <Metric> metrics = new PersistentList<Metric>(Collections.emptyList());
	private final PersistentList <TimedRecord> timedRecords= new PersistentList<TimedRecord>(Collections.emptyList());
	

	public PersistentData(){	
		// set up mechanism for persistence
	}
	
	
	public void loadPersistentData(){
	}
	
	//
	// Generic access methods for persistent objects
	//
	@SuppressWarnings("rawtypes")
	public PersistentList getDataList(EntityType type){
		switch (type) {
			case METER: return meters;
			case METRIC: return metrics;
			case TIMEDRECORD: return timedRecords; 
			default: return null;
		}
	}
	
	//
	// Access methods for individual persistent objects
	//
	
	public PersistentList<DeviceActivity> getActivity(){return activity;}

	public PersistentList<Meter> getMeters(){return meters;}

	public PersistentList<Metric> getMetrics(){return this.metrics;}

	public PersistentList<TimedRecord> getTimedRecords(){return timedRecords;}
	
}
