package org.ladbury.meterPkg;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


import org.ladbury.meterPkg.Metric.MetricType;
import org.ladbury.persistentData.Persistable;
import org.ladbury.userInterfacePkg.UiStyle;
//import org.ladbury.abodePkg.Abode;


public class Meter	implements	Serializable,
								Persistable <Meter>,
								Comparable<Meter>,
								Comparator<Meter>  {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5911387336252034382L;

	public static enum MeterType {UNDEFINED, OWLCM160, ONZO};
	
	private long meterId;			// persistence ID field
	private String name;			// the name of the meter
	private MeterType type;			// the meter type - which defines what metrics are stored
	private List<Metric> metrics;
//	private Abode abode;
	static final String ONZODATEFORMAT = "yyyy-MM-dd HH:mm:ss";

	//
	// Constructors
	//
	
	public Meter(){
		this.name = UiStyle.UNNAMED;
		this.type = MeterType.UNDEFINED;
		this.metrics = new ArrayList<Metric>(Collections.<Metric>emptyList());
//		this.abode = null;
	}
	public Meter(/*Abode a,*/MeterType t){
		
		this.name = UiStyle.UNNAMED;
		this.metrics = new ArrayList<Metric>(Collections.<Metric>emptyList());
		this.type = t;
//		this.abode = a;
		switch (t){
		case OWLCM160:{ //POWER_LOW_RES
			this.name = "OWL CM160";
			addMetric(MetricType.POWER_LOW_RES);
		}
		case ONZO: { //ENERGY_LOW_RES, ENERGY_HIGH_RES, POWER_REAL_STANDARD, POWER_REAL_FINE, POWER_REACTIVE_STANDARD
			this.name = "ONZO";
			addMetric(MetricType.ENERGY_LOW_RES);
			addMetric(MetricType.ENERGY_HIGH_RES);
			addMetric(MetricType.POWER_REAL_STANDARD);
			addMetric(MetricType.POWER_REAL_FINE);
			addMetric(MetricType.POWER_REACTIVE_STANDARD);
		}
		default: 
		}
	}
	//
	// Access methods
	//
	
	/**
	 * getType
	 * @return the type of the meter
	 */
	public MeterType getType() {
		return type;
	}

	/**
	 * setType
	 * @param type the type value to be set
	 */
	public void setType(MeterType type) {
		this.type = type;
	}
	

	public void setName(String n){
		this.name = n;
	}

	
	/**
	 * get Metric
	 * @param metricT the type of the metric to be returned
	 * @return the first of the meter's recoded metrics which matches the metric type
	 */
	public Metric getMetric(MetricType metricT) {
		for(int i=0; i<this.metrics.size();i++){
			if (metrics.get(i).getType() == metricT) return metrics.get(i);
		}
		return null;
	}
	/**
	 * Remove Metric
	 * @param metricT - the type of Metric
	 * @return True if a metric of the specified type was successfully removed
	 */
	public boolean removeMetric(MetricType metricT){
		for(int i=0; i<this.metrics.size();i++){
			if (metrics.get(i).getType() == metricT)
				return (metrics.remove(i)!=null);
		}
		return false;
	}
	/**
	 * Add Metric - appends the metric to the meter's store of metrics
	 * @param metricT
	 * @return
	 */
	public boolean addMetric(MetricType metricT){
		return metrics.add(new Metric(this,metricT));
	}
	/**
	 * Set Metric - replaces a metric already in the list of Metrics
	 * @param t
	 * @param metric
	 * @return True if the metric was successfully replaced
	 */
	public boolean setMetric(MetricType t, Metric metric) {
		int index = getMetricIndex(t); // find the first metric of this type
		if ( index == -1) return false;
		return this.metrics.set(index, metric) != null;
	}
	/**
	 * Get Metric index
	 * @param metricT
	 * @return the index of the first metric of the specified type or -1 if not found
	 */
	private int getMetricIndex(MetricType metricT) {
		for(int i=0; i<this.metrics.size();i++){
			if (metrics.get(i).getType() == metricT) return i;
		}
		return -1;
	}
	
	public int getMetricCount(){
		return metrics.size();
	}
	public Metric getMetric(int i){
		if(i>=0 && i< metrics.size()) return metrics.get(i);
		
		return null;
	}
	//
	// Methods to implement interfaces
	//

	@Override
	public int compare(Meter arg0, Meter arg1) {
		return arg0.compareTo(arg1);
	}

	@Override
	public int compareTo(Meter arg0) {
		return this.type.compareTo(arg0.type);
	}

	@Override
	public long id() {
		return meterId;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public String toCSV() {
		return 	meterId +","+
				name +","+
				type;
	}

	@Override
	public void updateFields(Meter element) {
		this.name = element.name;
		this.type = element.type;
		this.metrics = element.metrics;
//		this.abode = element.abode;
	}

	@Override
	public String idString() {
		return "["+this.id()+"] "+this.name();
	}
	/*
	public Abode getAbode() {
	    return abode;
	}
	public void setAbode(Abode param) {
	    this.abode = param;
	}*/
}
