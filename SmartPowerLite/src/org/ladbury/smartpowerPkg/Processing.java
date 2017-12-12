package org.ladbury.smartpowerPkg;


import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.ladbury.deviceActivityPkg.DeviceActivity;
import org.ladbury.meterPkg.Metric;
import org.ladbury.meterPkg.TimedRecord;
import org.ladbury.persistentData.PersistentData;
/**
 * This class provides methods for processing the data, essentially the application logic
 * these methods a typically called from the main processing loop in SmartPower
 * 
 * @author GJWood
 * @version 1.0 2012/11/29 Initial version (methods moved from SmartPower.java)
 * @version 2.0 2013/11/27 Converted for use with Onzo meter
 * @version 2.1 2013/11/29 functions solely involving readings held in Metric moved into Metric class
 * @version 2.2 2013/11/29 the Utility class has been incorporated in this class now
 * @version 2.3 2013/12/22 Constants made private
 * @see SmartPower
 */
public class Processing {
	
	//
	// Default constants for parameters
	//
	private static final double DEVICE_AMP_TOLERANCE = 0.004; 			// tolerance within which two doubles are "equal"  in readings
	private static final double DEVICE_AMP_EQUAL = 0.004; 				// tolerance within which two doubles are "identical"  in readings
	private static final int DEVICE_WATT_TOLERANCE = 5;					// tolerance within which two devices are "equal" (more precise because less noise)
	private static final int SQUELCH_THRESHOLD = 1; 					// the number of intervals which would be considered a transient;
	private static final int PLATEAU_THRESHOLD = SQUELCH_THRESHOLD+1; 	// the number of intervals which would be considered stable plateau, must be > transient;
	private static final int MATCHING_PERCENTAGE_TOLERANCE = 15;			// the default maximum % difference for a match between two values
    private static final int RECOGNITION_THRESHOLD = 100;				// the minimum delta that is recognised as a device

    private static int recognitionThreshold = RECOGNITION_THRESHOLD;
    private static int squelchThreshold = SQUELCH_THRESHOLD;
	private static int MatchingPercentageTolerance = MATCHING_PERCENTAGE_TOLERANCE;
    
    
    //
    // Processing parameter access
    //
    
    
	public static int getRecognitionThreshold() {
		return recognitionThreshold;
	}

	public static void setRecognitionThreshold(int recognitionThreshold) {
		Processing.recognitionThreshold = recognitionThreshold;
	}

	public static int getSquelchThreshold() {
		return squelchThreshold;
	}
	public static int getPlateauThreshold() {
		return PLATEAU_THRESHOLD;
	}

	public static void setSquelchThreshold(int squelchThreshold) {
		Processing.squelchThreshold = squelchThreshold;
	}

	public static int getMatchingPercentageTolerance() {
		return MatchingPercentageTolerance;
	}

	public static void setMatchingPercentageTolerance(
			int matchingPercentageTolerance) {
		MatchingPercentageTolerance = matchingPercentageTolerance;
	}

	//
    // matching procedures, NB signs are ignored
    //
	
	/**
	 * equal (Double)
	 * determines if two numbers are equal within a default absolute tolerance
	 * @param d1 first number
	 * @param d2 second number
	 * @return True if the numbers match
	 */
	public static boolean equal(double d1, double d2){ 
		return Math.abs(Math.abs(d1)-Math.abs(d2)) < DEVICE_AMP_EQUAL; 
	}
	
	/**
	 * matches (Double)
	 * determines if two numbers are matched within a default absolute tolerance
	 * @param d1 first number
	 * @param d2 second number
	 * @return True if the numbers match
	 */
	public static boolean matches(double d1, double d2){ 
		return Math.abs(Math.abs(d1)-Math.abs(d2)) < DEVICE_AMP_TOLERANCE; 
	}

	/**
	 * matches (integer)
	 * determines if two numbers are matched within a default absolute tolerance
	 * @param i1 first number
	 * @param i2 second number
	 * @return True if the numbers match
	 */
	public static boolean matches(int  i1, int i2){ 
		return Math.abs(Math.abs(i1)-Math.abs(i2)) < DEVICE_WATT_TOLERANCE; 
	}
	
	/**
	 * matchesPercent (integer)
	 * determines if two numbers are matched within a default percentage tolerance
	 * @param i1 first number
	 * @param i2 second number
	 * @return True if the numbers match
	 */
	private static boolean matchesPercent(int i1, int i2){
		double percentage;
		percentage = (double)(Math.abs(Math.abs(i1)-Math.abs(i2)))/(double)Math.max(i1, i2)*100.0;
		return Math.rint(percentage) <= MatchingPercentageTolerance; 
	}
	
	/**
	 * matchesPercent (integer)
	 * determines if two numbers are matched within a supplied percentage tolerance
	 * @param i1 first number
	 * @param i2 second number
	 * @param pc % tolerance (1-100)
	 * @return True if the numbers match
	 */
	public static boolean matchesPercent(int  i1, int i2, int pc){ 
		double percentage;
		percentage = (double)(Math.abs(Math.abs(i1)-Math.abs(i2)))/(double)Math.max(i1, i2)*100.0;
		return Math.rint(percentage) <= pc; 
	}
	
	//
	// Data processing
	//
	
	/**
	 * matchAndSaveActivity
	 * match reading events with similar consumption values
	 * and save matched events as device activity persistently
	 * This one for the Onzo meter
	 * @param m The metric containing the readings to be processed
	 */
	public static void matchAndSaveActivity(Metric m){
		TimedRecord 	rEvt, matchingEvt;
		DeviceActivity	dAct;
		
	    for (int i=0; i < m.size();i++){
	    	if (m.isOn(i) && (m.getDelta(i)>= recognitionThreshold)){
	    		rEvt = m.getRecord(i);
	    		// look for matching off
	    		matchingEvt = null;
	    		for (int j=i+1; j < m.size(); j++){
	    			if (matchesPercent(m.getDelta(i),
	    					m.getDelta(j))){
	    				matchingEvt = m.getRecord(j);
	    				break; //found it - exit loop
	    			}
	    		}
	    		if (matchingEvt != null){
	    			dAct = new DeviceActivity(rEvt.timestamp(), matchingEvt.timestamp(),m.getDelta(i),null);
	    			SmartPower.getInstance().getData().getActivity().add(dAct);
					SmartPower.getInstance().getFrame().displayLog("Device found "+dAct.toCSV()+"\n\r");

	    			// create TimePeriod / CalendarPeriod / Coincidence
	    		}
	    	}
	    }
	}
    
	public static XYDataset getDeviceActivityScatterData() {
        XYSeriesCollection result = new XYSeriesCollection();
        XYSeries series = new XYSeries("Device Activity");
        for (int i = 0; i < getData().getActivity().size(); i++) {
            double x = getData().getActivity().get(i).getDuration();
            double y = getData().getActivity().get(i).getConsumption();
            series.add(x, y);
        }
        result.addSeries(series);
        return result;
    }
	/**
	 * Access method for persistent data
	 * @return	the class holding the data in PersistentList form
	 * @see SmartPower
	 * @see PersistentData
	 */
	private static PersistentData getData(){
		return SmartPower.getInstance().getData();
	}
}
