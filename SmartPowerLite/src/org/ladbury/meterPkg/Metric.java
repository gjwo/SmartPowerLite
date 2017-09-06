package org.ladbury.meterPkg;

import java.io.PrintWriter;
import java.io.Serializable;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;


import org.jfree.data.time.SimpleTimePeriod;
import org.jfree.data.time.TimePeriodValues;
import org.jfree.data.time.TimePeriodValuesCollection;
import org.jfree.data.xy.XYDataset;
import org.ladbury.persistentData.Persistable;
import org.ladbury.smartpowerPkg.Processing;
import org.ladbury.smartpowerPkg.SmartPower;
import org.ladbury.smartpowerPkg.Timestamped;
import org.ladbury.userInterfacePkg.UiStyle;
import org.ladbury.meterPkg.TimedRecord;

import static org.ladbury.meterPkg.Metric.MetricType.*;


public class Metric	implements	Serializable,
								Persistable <Metric>,
								Comparable<Metric>,
								Comparator<Metric>  {
	

	/**
	 * 
	 */
	private static final long serialVersionUID = -701995953205153989L;

	public enum MetricType {UNDEFINED, POWER_LOW_RES, ENERGY_LOW_RES, ENERGY_HIGH_RES, POWER_REAL_STANDARD,
		POWER_REAL_FINE, POWER_REACTIVE_STANDARD, POWER_REAL, POWER_APPERENT, POWER_REACTIVE, VOLTAGE_RMS, CURRENT}

	public enum Granularity {UNDEFINED, SECOND, TEN_SECOND, MINUTE, TEN_MINUTE, HOUR, DAY}

	public static final int[] GRAIN_INTERVALS =  {0,Timestamped.SECOND_IN_MS,
													Timestamped.SECOND_IN_MS*10,
													Timestamped.MINUTE_IN_MS,
													Timestamped.MINUTE_IN_MS*10,
													Timestamped.HOUR_IN_MS,
													Timestamped.DAY_IN_MS,}; //in milliseconds

	
	private long metricId;				// persistence ID field
	private String name;			// the name of the metric
	private MetricType type;			// the metric type - which defines what readings are stored
	private Granularity grain;
	private boolean cumulative;
	private Timestamp earliest; //note first, last and end are all SQL reserved words causing persistence errors if used as field names
	private Timestamp latest;
	
	// relationships with other entities
	private List<TimedRecord> readings;
	private Meter meter;

	//
	// Constructors
	//
	
	public Metric(){
		this.name = UiStyle.UNNAMED;
		this.type = MetricType.UNDEFINED;
		this.readings = new ArrayList<>(Collections.emptyList());
		this.grain = Granularity.UNDEFINED;
		this.cumulative = false;
		this.earliest = new Timestamp(0);
		this.latest = new Timestamp(0);
		this.meter = null;
	}
	public Metric(Meter m,MetricType t){
		//Owl metrics POWER
		//Onzo metrics ENERGY_LOW_RES, ENERGY_HIGH_RES, POWER_REAL_STANDARD, POWER_REAL_FINE, POWER_REACTIVE_STANDARD
		
		this.name = UiStyle.UNNAMED;
		this.readings = new ArrayList<>(Collections.emptyList());
		this.type = t;
		this.grain = Granularity.UNDEFINED;
		this.cumulative = false;
		this.earliest = new Timestamp(0);
		this.latest = new Timestamp(0);
		this.meter = m;

		this.setType(t);
		this.setGrain(Granularity.SECOND);
		switch (t){
		case POWER_LOW_RES:
			this.setName("Power");
			this.setGrain(Granularity.MINUTE);
			break;
		case ENERGY_LOW_RES:
			this.setCumulative(true);
			break;
		case ENERGY_HIGH_RES: 
			this.setName("Energy high resolution");
			this.setCumulative(true);
			break;
		case POWER_REAL_STANDARD: 
			this.setName("Power real standard");
			break;
		case POWER_REAL_FINE: 
			this.setName("Power real fine");
			break;
		case POWER_REACTIVE_STANDARD: 
			this.setName("Power reactive standard");
			break;
		case POWER_REAL:
			this.setName("Power real");
			break;
		case POWER_APPERENT:
			this.setName("Power apparent");
			break;
		case POWER_REACTIVE:
			this.setName("Power reactive");
			break;
		case VOLTAGE_RMS:
			this.setName("Voltage RMS");
			break;
		case CURRENT:
			this.setName("Current");
			break;
		default: this.setName("UNDEFINED");
		}
	}
	
	//
	// Functional methods to the readings accessed by timestamp
	//
	
	/**
	 * getRecord
	 * @param t timestamp of the record to be retrieved
	 * @return the record found or null if no record exists for that timestamp
	 */
	public TimedRecord getRecord(Timestamp t){	
		for (TimedRecord tr : readings){
			if (tr.timestamp() == t) return tr;
		}

		return null;
	}
	
	/**
	 * removeRecord
	 * @param t timestamp of the record to be removed
	 * @return true if successful
	 */
	public boolean removeRecord(Timestamp t){
		for(int i=0; i<readings.size(); i++){
		if (readings.get(i).timestamp().equals(t)){
				removeRecord(i);
				return true;
			}
		}
		return false;
	}
	
	/**
	* returns the record at row, or null if no actual record was recorded 
	* get the value recorded or inferred at time t
	*/
	public int getValue(Timestamp t){
		if (t.before(earliest)||t.after(latest)||(readings.size()<=0)) return -1; // won't find a value for the timestamp
		Iterator<TimedRecord> recIterator = readings.iterator();
		TimedRecord trBefore = readings.get(0);
		while (recIterator.hasNext()){
			if(((TimedRecord) recIterator).timestamp().equals(t)) return ((TimedRecord) recIterator).value(); //found it
			if (((TimedRecord) recIterator).timestamp().after(t)){ // passed the point were it would be found so interpolate			
				if(cumulative){
				//TODO need to work out slope etc here	
					return ((TimedRecord) recIterator).value();
				} else 	return trBefore.value();
			}		
			recIterator.next();
		}	
		return -1; //error if we get here!
	}
	

	/**
	 * getDelta
	 * get the difference in the value recorded or inferred at this time (t) and (t - grain)
	 * @param t timestamp
	 * @return the change in recorded value
	 */
	public int getDelta(Timestamp t){
		if (t.before(earliest)||t.after(latest)||(readings.size()<=1)) return -1; // won't find two values
		Iterator<TimedRecord> recIterator = readings.iterator();
		TimedRecord trBefore = readings.get(0);
		if (readings.get(1).timestamp().after(t)) return 0; //no change since the earliest value
		while (recIterator.hasNext()){
			recIterator.next(); // move to the second value
			if(((TimedRecord) recIterator).timestamp().equals(t)) return ((TimedRecord) recIterator).value()-trBefore.value(); //found it
			if (((TimedRecord) recIterator).timestamp().after(t)){ // passed the point were it would be found so interpolate			
				if(cumulative){
				//TODO need to work out slope etc here	
					return ((TimedRecord) recIterator).value();
				} else 	return 0; //no change value recorded;
			}		
			recIterator.next();
		}	
		return -1; //error if we get here!
	}
	
	/**
	 * getGraphData
	 * Gets data for graphing
	 * @param t1 Starting timestamp
	 * @param t2 Ending Timestamp
	 * @param g The granularity of data to be returned
	 * @return 	An array list of timed records one for each increment between and including the timestamps
	 * 			these records may be real recordings or interpolated results
	 */
	public ArrayList<TimedRecord> getGraphData(Timestamp t1, Timestamp t2,Granularity g){
		int i1=-1,i2=-1;
		int i;
		Timestamp incrementalTs,recordedTs;
		int recordedValue;
		ArrayList<TimedRecord> results = new ArrayList<>();
		if (t1.before(earliest)||t1.after(latest)||(readings.size()<=1)) return null; // won't find a value for the timestamp t1
		if (t2.before(earliest)||t2.after(latest)) return null; // won't find a value for the timestamp t2
		if (g.ordinal()<grain.ordinal()) return null; // can't service the request at this granularity
		
		for(i=0;i<readings.size();i++){
			if (readings.get(i).timestamp().equals(t1)){
				i1=i;
				break;
			}
			if (readings.get(i).timestamp().after(t1)){
				i1=i-1;
				break;
			}
		
		}
		results.add(readings.get(i1));
		incrementalTs=results.get(0).timestamp();
		recordedValue=readings.get(i1).value(); 
		for (i2=i1+1; i2<readings.size(); i2++){
			recordedTs=readings.get(i2).timestamp();
			if(recordedTs.after(t2)) return results;
			while(incrementalTs.before(recordedTs)){
				incrementalTs.setTime(incrementalTs.getTime() + Metric.GRAIN_INTERVALS[g.ordinal()]);
				if(cumulative){
					results.add(new TimedRecord(incrementalTs,recordedValue));
					//TODO need to work out slope etc here	
				} else 	results.add(new TimedRecord(incrementalTs,recordedValue));
			}
			recordedValue = readings.get(i2).value();
			results.add(new TimedRecord(recordedTs,recordedValue));
		}
		return results;
	}
	/**
	 * getJfreeChartData
	 * Gets data for graphing
	 * @param t1 Starting timestamp
	 * @param t2 Ending Timestamp
	 * @return 	An array list of timed records one for each increment between and including the timestamps
	 * 			these records may be real recordings or interpolated results
	 */
	public XYDataset getJfreeChartData(Timestamp t1, Timestamp t2){
		int index1=-1;
		Timestamp startTime,endTime;
		TimePeriodValues results = new TimePeriodValues(this.type.name());
        final TimePeriodValuesCollection results1 = new TimePeriodValuesCollection();
        
		if (t1.before(earliest)||t1.after(latest)||(readings.size()<=1)) return null; // won't find a value for the timestamp t1
		if (t2.before(earliest)||t2.after(latest)) return null; // won't find a value for the timestamp t2
		
		for(int i = 0;i<readings.size();i++){
			if (readings.get(i).timestamp().equals(t1)){
				index1=i;
				break;
			}
			if (readings.get(i).timestamp().after(t1)){
				index1=i-1;
				break;
			}		
		}
		// index1 now contains the index of the starting timestamp
		for (int i=index1; i<(readings.size()-1); i++){
			startTime = readings.get(i).timestamp();
			if(startTime.after(t2)) break;
			endTime = readings.get(i+1).timestamp();
			if (endTime.before(startTime))
				SmartPower.getMain().getFrame().displayLog("Bad Timestamps "+startTime + " - " + endTime+"\n\r");
			else results.add(new SimpleTimePeriod(startTime, endTime), readings.get(i).value());
		}
		results1.addSeries(results);
		return results1;
	}

	/**
	 * getEventData
	 * Gets data for graphing
	 * @param t1 Starting timestamp
	 * @param t2 Ending Timestamp
	 * @return 	An array list of timed records corresponding to the recorded
	 * 			events between and including the timestamps
	 */
	public Metric getEventData(Timestamp t1, Timestamp t2){
		int i1=-1,i2=-1;
		int i;
		Metric result = new Metric(this.meter, this.type);
		result.name = "Subset";
		if (t1.before(earliest)||t1.after(latest)||(readings.size()<=1)) return null; // won't find a value for the timestamp t1
		if (t2.before(earliest)||t2.after(latest)) return null; // won't find a value for the timestamp t2
		
		for(i=0;i<readings.size();i++){
			if (readings.get(i).timestamp().equals(t1)){
				i1=i;
				break;
			}
			if (readings.get(i).timestamp().after(t1)){
				i1=i-1;
				break;
			}
		
		}
		result.readings.add(readings.get(i1));
		for (i2=i1+1; i2<readings.size(); i2++){
			if(readings.get(i2).timestamp().after(t2)) return result;
			result.readings.add(readings.get(i2));
		}
		return result;
	}
	
	/**
	 * getDelta
	 * Get the difference in the value recorded or inferred at the two timed points
	 * @param t1 Starting timestamp
	 * @param t2 Ending Timestamp
	 * @return Time difference in Seconds
	 */
	public long getDelta(Timestamp t1, Timestamp t2){
		long v1=-1,v2=-1;
		Iterator<TimedRecord> recIterator = readings.iterator();
		if (t1.before(earliest)||t1.after(latest)||(readings.size()<=1)) return -1L; // won't find a value for the timestamp t1
		if (t2.before(earliest)||t2.after(latest)) return -1L; // won't find a value for the timestamp t2
		
		TimedRecord trBefore = readings.get(0);
		while (recIterator.hasNext()){
			if(v1==-1){
				if(((TimedRecord) recIterator).timestamp().equals(t1)) v1 = ((TimedRecord) recIterator).value(); //found it
				if (((TimedRecord) recIterator).timestamp().after(t1)){ // passed the point were it would be found so interpolate			
					if(cumulative){
						//TODO need to work out slope etc here	
						v1= ((TimedRecord) recIterator).value();
					} else 	v1= trBefore.value();
				}		
				recIterator.next();
			}
			else{
				if(((TimedRecord) recIterator).timestamp().equals(t2)) v2 = ((TimedRecord) recIterator).value() ; //found it
				if (((TimedRecord) recIterator).timestamp().after(t2)){ // passed the point were it would be found so interpolate			
					if(cumulative){
						//TODO need to work out slope etc here	
						v2= ((TimedRecord) recIterator).value();
					} else 	v2= trBefore.value();
				}		
				if (v2 !=-1) return v2-v1;
				recIterator.next();			
			}
		}	
		return -1L; //error if we get here!
	}
	/**
	 * getNextEvent
	 * @param t The timestamp to earliest from
	 * @return next timestamp at which the value is after than the supplied timestamp
	 */
	public Timestamp getNextEvent(Timestamp t){
		Iterator<TimedRecord> recIterator = readings.iterator();
		
		if (t.before(earliest))return null;
		while (recIterator.hasNext()){
			if (((TimedRecord) recIterator).timestamp().after(t))return ((TimedRecord) recIterator).timestamp();
			recIterator.next();
		}
		return null;
	}
	/**
	 * appendRecord
	 * appends a new record after the latest element at time r.timestamp()
	 * @param r the record to be recorded
	 * @return true if successful
	 */
	public boolean appendRecord(TimedRecord r){
		if (this.latest.after(r.timestamp())) return false; // This timestamp is before data already stored
		this.latest = r.timestamp();
		if (this.size()==0) this.earliest =  r.timestamp();
		readings.add(r);
		SmartPower.getMain().getData().getTimedRecords().add(r);
		return true;
	}
	
	/**
	 * findTimestamp
	 * @param t the Timestamp to look for
	 * @return the index of the timestamp if found or -1 if not found
	 */
	public int findTimestamp(Timestamp t){
		Iterator<TimedRecord> recIterator = readings.iterator();
		
		while (recIterator.hasNext()){
			if (((TimedRecord) recIterator).timestamp().equals(t)){
				return readings.indexOf(recIterator);
			}
			recIterator.next();
		}
		return -1;
	}
	
	/**
	 * clearReadings
	 * removes all readings between the two times and resets earliest and/or latest times as appropriate
	 * @param t1
	 * @param t2
	 */
	public void clearReadings(Timestamp t1, Timestamp t2){
		if (t1==null || t2==null ) return; // bad parameters
		if(t1.after(t2))return; // bad parameters
		
		for (int i= readings.size()-1;i>=0;i--){ // work backwards to avoid the indexes changing as we delete elements
			if (readings.get(i).timestamp().before(t1)) break;
			if (readings.get(i).timestamp().before(t2)) removeRecord(i); // we already know it's not before t1
			else if (readings.get(i).timestamp().equals(t1))removeRecord(i);
		}
		
		if (readings.size() > 0){
			earliest = readings.get(0).timestamp();
			latest = readings.get(readings.size()-1).timestamp();
		} else{
			earliest = new Timestamp(0);
			latest = new Timestamp(0);
		}
	}
	/**
	 * clearReadings
	 * removes all readings between the two times and resets earliest and latest times to initial values
	*/
	public void clearAllReadings(){
		for (int i= readings.size()-1;i>=0;i--){ 
			removeRecord(i);		
		}
		earliest = new Timestamp(0);
		latest = new Timestamp(0);
	}
    /**
    * Change slopes to cliffs by eliminating
    * the single transitional and spike values 
    */
    public  void squelchTransitions() {
    	
     	int intervalInMs = Metric.getGrainIntervals()[grain.ordinal()];
		//SmartPower.getMain().getFrame().displayLog("interval = "+ interval+ "ms\n\r");
     	for (int i = this.size()-2; i>0; i--){
			//traceRecord("Loop",i);
    		if( this.getIntervals(i)<=Processing.getSquelchThreshold()){
    			traceRecord("Singleton",i);
    			// we have a transition or isolated spike or isolated dip
    			// in the future we should record this in a new data structure
    			
     			// to qualify as a plateau the interval at a given value must be >= the plateau threshold
    			// now check i-1 and i+1 qualify as plateaus
    			if ((this.getIntervals(i-1)>=Processing.getPlateauThreshold()) &&
    				(this.getIntervals(i+1)>=Processing.getPlateauThreshold())){
    				// we have two plateaus surrounding a singleton
    				// eliminate the anomaly adding 1 interval to the greater of the surrounding plateaus and by removing singleton
    				// NB processing order is important as removal changes the indexes
        			//traceRecord("Plateau",i-1);
        			//traceRecord("Plateau",i+1);				
    				if(this.getRecord(i-1).value() < this.getRecord(i+1).value()){
    					// the trailing plateau needs to be extended forwards by 1 interval
    					this.getRecord(i+1).setTimestamp(new Timestamp(this.getRecord(i+1).timestamp().getTime()-intervalInMs));
    				} //else by removing the record we will extend the time intervals of the previous record
    				
    				if( !this.removeRecord(i)){
    					SmartPower.getMain().getFrame().displayLog("remove failed in squelch\n\r");
    					return; //bail out if remove failed;
    				}
    			}
    		}
     	}
    }
	/**
	 * Remove redundant data
	 */
	public void removeRedundantData(){
		for(int i = this.size()-1; i>1; i--){
			if(this.getRecord(i).value() == this.getRecord(i-1).value())
				if( !this.removeRecord(i)){
					SmartPower.getMain().getFrame().displayLog("remove failed ");
					return; //bail out if remove failed;
				}
		}
	}	

	//
	// Access methods to the readings by array index (aka row)
	//
	
	/**
	 * getDelta
	 * @param row the row to earliest from
	 * @return a difference in values between this row and the previous one
	 */
	public int getDelta(int row){
		if (row<0 || row>= readings.size()) return 0;
		if (row>0) return readings.get(row).value()-readings.get(row-1).value();
		return 0;
	}
	
	/**
	 * isOn
	 * @param row the row to earliest from
	 * @return true if this reading represents a device turning on
	 */
	public boolean isOn(int row){
		return (getDelta(row)>0);		
	}
	/**
	 * getIntervals
	 * @param row the reading to earliest from
	 * @return How many ticks to the next reading
	 */
	public int getIntervals(int row){
		int interval; 
		Timestamp ts = new Timestamp(0);
		if (row < 0 || row >= (readings.size()-1)) return 0;
		
		ts.setTime(readings.get(row+1).timestamp().getTime() - readings.get(row).timestamp().getTime());
		interval = (int)  (ts.getTime() / (Metric.GRAIN_INTERVALS[grain.ordinal()]));
		//SmartPower.getMain().getFrame().displayLog(interval+" Interval\n\r");
		
		return interval;
	}
	/**
	 * removeRecord
	 * @param row the index of record to remove
	 * @return true if successful
	 */
	public boolean removeRecord(int row){
		if (row<1 || row>= readings.size()){
			SmartPower.getMain().getFrame().displayLog("removeRecord range check failed row["+row+"]\n\r");
			return false;
		}
		if (readings.remove(row)==null){ //remove local copy
			SmartPower.getMain().getFrame().displayLog("row["+row+"] remove failed\n\r");
			return false;
		}
		//SmartPower.getMain().getFrame().displayLog("row["+row+"] local copy removed\n\r");
		if (SmartPower.getMain().getData().getTimedRecords().remove(row)==null){ //remove persistent data
			SmartPower.getMain().getFrame().displayLog("TimedRecords.remove failed row["+row+"]\n\r");
			return false;
		}
		//SmartPower.getMain().getFrame().displayLog("row["+row+"] persistent copy removed\n\r");
		return true;
	}

	/**
	 * getRecord
	 * @param row the index of the record to get
	 * @return the record from the specified row or null if out of range
	 */
	public TimedRecord getRecord(int row){	
		if (row<0 || row>= readings.size()) return null;
		return readings.get(row);
	}
	
    //
    // output a record to the log
    //
    public void traceRecord(String s,int row){
		SmartPower.getMain().getFrame().displayLog(
				s+" Row ["+row+
				"] Timestamp "+this.getRecord(row).timestampString()+
				" Intervals["+this.getIntervals(row)+
				"] Value "+this.getRecord(row).value()+"]\n\r");
    }
    
    //
    // output the set of readings (Comma Separated Variables)
    //
    public void outputReadingsCSV(PrintWriter pw) {
		DateFormat df = new SimpleDateFormat(Timestamped.OUTPUTDATEFORMAT);
		TimedRecord r;
    	for(int i=0; i<readings.size();i++){
    		r = readings.get(i);
    		pw.printf("%d,%s,%d,%d,%d", r.id(),df.format(r.timestamp()), r.value(),getIntervals(i),getDelta(i));
    		pw.println();
    	}
    }
	
	//
	// simple Access methods to stored data
	//
	public int size(){
		return readings.size();
	}
	
	/**
	 * getType
	 * @return the type of the metric
	 */
	public MetricType getType() {
		return type;
	}

	public void setName(String n){
		this.name = n;
	}

	/**
	 * setType
	 * @param type the type value to be set
	 */
	public void setType(MetricType type) {
		this.type = type;
	}
	public void setReadings(ArrayList<TimedRecord> r) {readings = r;}
	public Meter getMeter() {
	    return meter;
	}
	public void setMeter(Meter param) {
	    this.meter = param;
	}
	public Granularity getGrain() {
		return grain;
	}
	public static long getSerialversionUID() {return serialVersionUID;}
	public static int[] getGrainIntervals() {
		return GRAIN_INTERVALS;
	}
	public String getName() {
		return name;
	}
	public Timestamp getEarliest() {
		return earliest;
	}
	public Timestamp getLatest() {
		return latest;
	}
	public void setGrain(Granularity grain) {
		this.grain = grain;
	}
	public boolean isCumulative() {
		return cumulative;
	}
	public void setCumulative(boolean cumulative) {
		this.cumulative = cumulative;
	}
	
	/**
	 * get Readings
	 * @return one of the metric's recoded readings
	 */
	public List<TimedRecord> getReadings() {
		return readings;
	}
	public int getReadingsCount(){
		return readings.size();
	}
    //
    // construct a systematic name for
    // this set of readings
    //
    public String readingsName(){
		DateFormat df = new SimpleDateFormat(Timestamped.COMPACTDATEFORMAT);
    	return meter.name()+"_"+type.toString()+"_"+df.format(earliest)+"-"+df.format(latest);
    }

	//
	// Methods to implement interfaces
	//

	@Override
	public int compare(Metric arg0, Metric arg1) {
		return arg0.compareTo(arg1);
	}

	@Override
	public int compareTo(Metric arg0) {
		return this.type.compareTo(arg0.type);
	}

	@Override
	public long id() {
		return metricId;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public String toCSV() {
		return 	metricId +","+
				name +","+
				type;
	}

	@Override
	public void updateFields(Metric element) {
		this.name = element.name;
		this.type = element.type;
		this.grain = element.grain;
		this.cumulative = element.cumulative;
		this.earliest = element.earliest;
		this.latest = element.latest;
		this.meter = element.meter;
		this.readings = element.readings;
	}

	@Override
	public String idString() {
		return "["+this.id()+"] "+this.name();
	}
}
