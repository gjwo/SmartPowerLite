package org.ladbury.dataServicePkg;

/**
 * SmartPowerLite
 * Created by Matthew Wood on 07/09/2017.
 */
public class DataServiceMeter
{
    private final String displayName;
    private final String tag;
    private final String description;

    public DataServiceMeter(String displayName, String tag, String description)
    {
        this.displayName = displayName;
        this.tag = tag;
        this.description = description;
    }

    public String getDisplayName()
    {
        return displayName;
    }

    public String getTag()
    {
        return tag;
    }

    public String getDescription()
    {
        return description;
    }

    @Override
    public String toString()
    {
        return displayName;
    }
}
