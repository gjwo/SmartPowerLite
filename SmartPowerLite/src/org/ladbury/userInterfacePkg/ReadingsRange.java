package org.ladbury.userInterfacePkg;

import me.mawood.data_api_client.objects.DataType;
import me.mawood.data_api_client.objects.Device;

import java.time.Instant;
import java.util.Date;

public class ReadingsRange
{
    private Device device;
    private DataType dataType;
    private Date earliestTime;
    private Date latestTime;

    ReadingsRange(Device device, DataType dataType)
    {
        this.device = device;
        this.dataType = dataType;
        this.earliestTime = Date.from(Instant.ofEpochMilli(0L));
        this.latestTime = Date.from(Instant.now());
    }
    ReadingsRange(Device device, DataType dataType, Date earliestTime, Date latestTime )
    {
        this.device = device;
        this.dataType = dataType;
        this.earliestTime = earliestTime;
        this.latestTime = latestTime;
    }
    ReadingsRange()
    {
        this.device = new Device();
        this.dataType = new DataType();
        this.earliestTime = new Date();
        this.latestTime = new Date();
    }
    public Device getDevice()
    {
        return device;
    }
    public DataType getDataType()
    {
        return dataType;
    }
    public Date getEarliestTime()
    {
        return earliestTime;
    }
    public Date getLatestTime()
    {
        return latestTime;
    }
    public void setDevice(Device device)
    {
        this.device = device;
    }
    public void setDataType(DataType dataType)
    {
        this.dataType = dataType;
    }
    public void setEarliestTime(Date earliestTime)
    {
        this.earliestTime = earliestTime;
    }
    public void setLatestTime(Date latestTime)
    {
        this.latestTime = latestTime;
    }
}
