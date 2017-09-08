package org.ladbury.meterPkg;

public enum MetricType
{
    UNDEFINED ("Undefined","?","?"),
    POWER_LOW_RES ("","Real Power(low res)","W"),
    ENERGY_LOW_RES ("","Energy(low res)","WH"),
    ENERGY_HIGH_RES ("","Energy","WH"),
    POWER_REAL_STANDARD ("","Real Power","W"),
    POWER_REAL_FINE ("","Real Power(fine)","W"),
    POWER_REACTIVE_STANDARD ("","Reactive Power","VAR"),
    //PMon10 types have a tag for accessing the API
    POWER_REAL ("realpower", "Real Power","W"),
    POWER_APPARENT ("apparentpower", "Apparent Power","VA"),
    POWER_REACTIVE ("reactivepower", "Reactive Power","VAR"),
    VOLTAGE_RMS ("voltage", "Voltage (RMS)","V"),
    CURRENT ("current","Current","A");
private final String tag;
private final String name;
private final String units;
MetricType(String tag, String name, String units)
{
    this.tag = tag;
    this.name = name;
    this.units = units;
}
public String getTag(){return this.tag;}
public String getName(){return this.name;}
public String getUnits(){return this.units;}
public static MetricType getMetricTypeFromTag(String tag)
    {
        for (MetricType metricType : values())
        {
            if (metricType.getTag().equalsIgnoreCase(tag))
            {
                return metricType;
            }
        }
        return null;
    }

}
