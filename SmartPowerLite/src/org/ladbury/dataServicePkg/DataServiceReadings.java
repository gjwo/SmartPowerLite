package org.ladbury.dataServicePkg;

import org.ladbury.meterPkg.TimestampedDouble;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DataServiceReadings
{
    private Collection<TimestampedDouble> readings;
    private DataServiceMeter meter;
    private DataServiceMetric metric;
    private Instant startTime;
    private Instant endTime;

    DataServiceReadings()
    {
        meter = new DataServiceMeter("Undefined","",""); //The data service meter that generated this metric
        metric = new DataServiceMetric("Undefined","","", "");
        startTime = Instant.ofEpochSecond(0);
        endTime = Instant.now();
        readings = new ArrayList<>();
    }
    public void add(TimestampedDouble reading)
    {
        readings.add(reading);
    }

    public void printReadings()
    {
        for(TimestampedDouble reading: readings)
        {
            System.out.println(reading);
        }
    }

    public Collection<String> getReadingsAsStrings()
    {
        List<String> results = new ArrayList<>();
        for(TimestampedDouble reading: readings)
        {
            results.add(reading.toString());
        }
        return results;
    }

    public Collection<TimestampedDouble> getReadings()
    {
        return readings;
    }
    public DataServiceMeter getMeter()
    {
        return meter;
    }
    public DataServiceMetric getMetric()
    {
        return metric;
    }
    public Instant getStartTime()
    {
        return startTime;
    }
    public Instant getEndTime()
    {
        return endTime;
    }
    public void setMeter(DataServiceMeter meter)
    {
        this.meter = meter;
    }
    public void setMetric(DataServiceMetric metric)
    {
        this.metric = metric;
    }
    public void setStartTime(Instant startTime)
    {
        this.startTime = startTime;
    }
    public void setEndTime(Instant endTime)
    {
        this.endTime = endTime;
    }
}
