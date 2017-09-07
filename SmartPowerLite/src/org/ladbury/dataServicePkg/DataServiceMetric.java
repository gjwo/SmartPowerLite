package org.ladbury.dataServicePkg;

/**
 * SmartPowerLite
 * Created by Matthew Wood on 07/09/2017.
 */
public class DataServiceMetric
{
    private final String displayName;
    private final String tag;
    private final String symbol;
    private final String description;

    public DataServiceMetric(String displayName, String tag, String symbol, String description)
    {
        this.displayName = displayName;
        this.tag = tag;
        this.symbol = symbol;
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

    public String getSymbol()
    {
        return symbol;
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
