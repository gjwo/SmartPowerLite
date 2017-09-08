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


@SuppressWarnings("SpellCheckingInspection")
public class Meter	implements	Serializable,
								Persistable <Meter>,
								Comparable<Meter>,
								Comparator<Meter>  {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5911387336252034382L;

	@SuppressWarnings("SpellCheckingInspection")
	public enum MeterType {UNDEFINED, OWLCM160, ONZO, PMON10}

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
		this.metrics = new ArrayList<>(Collections.emptyList());
//		this.abode = null;
	}
	public Meter(/*Abode a,*/MeterType t){
		
		this.name = UiStyle.UNNAMED;
		this.metrics = new ArrayList<>(Collections.emptyList());
		this.type = t;
//		this.abode = a;
		switch (t){
		case OWLCM160:{ //POWER_LOW_RES
			this.name = "OWL CM160";
			addMetric(MetricType.POWER_LOW_RES);
			break;
		}
		case ONZO: { //ENERGY_LOW_RES, ENERGY_HIGH_RES, POWER_REAL_STANDARD, POWER_REAL_FINE, POWER_REACTIVE_STANDARD
			this.name = "ONZO";
			addMetric(MetricType.ENERGY_LOW_RES);
			addMetric(MetricType.ENERGY_HIGH_RES);
			addMetric(MetricType.POWER_REAL_STANDARD);
			addMetric(MetricType.POWER_REAL_FINE);
			addMetric(MetricType.POWER_REACTIVE_STANDARD);
			break;
		}
		case PMON10: { //POWER_REAL, POWER_APPARENT, POWER_REACTIVE, VOLTAGE_RMS, CURRENT
			this.name = "PMon10"; //overwridden later with circuit each circuit is treated as a meter
			addMetric(MetricType.POWER_REAL);
			addMetric(MetricType.POWER_APPARENT);
			addMetric(MetricType.POWER_REACTIVE);
			addMetric(MetricType.VOLTAGE_RMS);
			addMetric(MetricType.CURRENT);
			break;
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
	 * @param metricType the type of the metric to be returned
	 * @return the first of the meter's recoded metrics which matches the metric type
	 */
	public Metric getMetric(MetricType metricType) {
		for (Metric metric : this.metrics)
		{
			if (metric.getType() == metricType) return metric;
		}
		return null;
	}
	/**
	 * Remove Metric
	 * @param metricType - the type of Metric
	 * @return True if a metric of the specified type was successfully removed
	 */
	public boolean removeMetric(MetricType metricType){
		for(int i=0; i<this.metrics.size();i++){
			if (metrics.get(i).getType() == metricType)
				return (metrics.remove(i)!=null);
		}
		return false;
	}
	/**
	 * Add Metric - appends the metric to the meter's store of metrics
	 * @param metricType	The metric type to be added
	 * @return	true if added succesfully
	 */
	public boolean addMetric(MetricType metricType){
		return metrics.add(new Metric(this,metricType));
	}
	/**
	 * Set Metric - replaces a metric already in the list of Metrics
	 * @param metricType 		Metric Type
	 * @param metric	Metric value
	 * @return True if the metric was successfully replaced
	 */
	public boolean setMetric(MetricType metricType, Metric metric)
	{
		int index = getMetricIndex(metricType); // find the first metric of this type
		return index != -1 && this.metrics.set(index, metric) != null;
	}
	/**
	 * Get Metric index
	 * @param metricType 		Metric Type
	 * @return the index of the first metric of the specified type or -1 if not found
	 */
	private int getMetricIndex(MetricType metricType) {
		for(int i=0; i<this.metrics.size();i++){
			if (metrics.get(i).getType() == metricType) return i;
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
