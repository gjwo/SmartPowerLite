package org.ladbury.dataServicePkg;

import java.time.Instant;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.json.JSONArray;
import org.json.JSONObject;
import org.ladbury.meterPkg.TimestampedDouble;

import java.util.*;

public class DataService // copied from DBRestAPI in MQTTListener
{
    private static final String DEFAULT_API_URL = "http://192.168.1.127:8080/";
    private static final String DEVICES = "devices/"; //for list of devices
    private static final String DEVICE = "device/"; //for specific device commands
    private static final String DATA_TYPES = "datatypes/"; //for list of dataTypes
    private static final int    REST_REQUEST_SUCCESSFUL = 200;

    private final HashMap<String, WebResource> resources;
    private final Client restClient;
    private int lastRestError;
    private ClientResponse clientResponse;
    private final String apiUrl;
    private Collection<DataServiceMeter> meters;
    private Collection<DataServiceMetric> metrics;
    private DataServiceReadings readings;

    @SuppressWarnings("WeakerAccess")
    public DataService(String apiUrl)
    {
        resources = new HashMap<>();
        clientResponse = null;
        meters = new ArrayList<>();
        metrics = new ArrayList<>();
        restClient = Client.create();
        lastRestError = REST_REQUEST_SUCCESSFUL;
        if ((apiUrl == null) || (apiUrl.isEmpty()))
            this.apiUrl =  DEFAULT_API_URL;
        else this.apiUrl = apiUrl;
    }

    public DataService()
    {
        this(DEFAULT_API_URL);
    }

    private synchronized WebResource getResource(String resource)
    {
        //System.out.println(this.apiUrl +resource);
        if(!resources.containsKey(resource))
            resources.put(resource, restClient.resource(this.apiUrl + resource));
        return resources.get(resource);
    }

    public Collection<DataServiceMeter> refreshMetersFromDB()
    {
        clientResponse = getResource(DEVICES).get(ClientResponse.class);
        lastRestError = clientResponse.getStatus();
        if (lastRestError != REST_REQUEST_SUCCESSFUL)
        {
            printLastError();
            return new ArrayList<>();
        }

        else
        {
            JSONObject response = new JSONObject(clientResponse.getEntity(String.class));
            JSONArray resultArray = response.getJSONArray("data");
            meters = new ArrayList<>();
            for (int i = 0; i < resultArray.length(); i++)
            {
                meters.add(new DataServiceMeter(
                        resultArray.getJSONObject(i).getString("name"),
                        resultArray.getJSONObject(i).getString("tag"),
                        resultArray.getJSONObject(i).getString("description")));
            }
            return meters;
        }
    }

    public Collection<String> getMeterNames()
    {
        List<String> results = new ArrayList<>();
        for (DataServiceMeter meter:meters )
        {
            results.add(meter.getDisplayName());
        }
        return results;
    }

    public Collection<DataServiceMetric> refreshMetricsFromDB()
    {
        clientResponse = getResource(DATA_TYPES).get(ClientResponse.class);
        lastRestError = clientResponse.getStatus();
        if (lastRestError != REST_REQUEST_SUCCESSFUL)
        {
            printLastError();
            return new ArrayList<>();
        }

        else
        {
            JSONObject response = new JSONObject(clientResponse.getEntity(String.class));
            JSONArray resultArray = response.getJSONArray("data");
            metrics = new ArrayList<>();
            for (int i = 0; i < resultArray.length(); i++)
            {
                metrics.add(new DataServiceMetric(
                        resultArray.getJSONObject(i).getString("name"),
                        resultArray.getJSONObject(i).getString("tag"),
                        resultArray.getJSONObject(i).getString("symbol"),
                        resultArray.getJSONObject(i).getString("description")));
            }
            return metrics;
        }
    }
    public Collection<String> getMetricNames()
    {
        List<String> results = new ArrayList<>();
        for (DataServiceMetric metric:metrics )
        {
            results.add(metric.getDisplayName());
        }
        return results;
    }

    public Instant getEarliestReading()
    {
        return readings.getStartTime();
    }
    public Instant getLatestReading()
    {
        return readings.getEndTime();
    }

    public DataServiceReadings refreshMetricForPeriodFromDB(DataServiceMeter meter, DataServiceMetric metric, Instant start, Instant end)
    {
        readings = new DataServiceReadings();
        clientResponse = getResource(DEVICE+meter.getTag()+"/"+metric.getTag()+"/")
                .queryParam("start", ((Long) start.toEpochMilli()).toString())
                .queryParam("end", ((Long) end.toEpochMilli()).toString())
                .get(ClientResponse.class);
        lastRestError = clientResponse.getStatus();
        if (lastRestError != REST_REQUEST_SUCCESSFUL)
        {
            printLastError();
            return readings;
        }
        else
        {
            readings.setStartTime(start);
            readings.setEndTime(end);
            readings.setMeter(meter);
            readings.setMetric(metric);
            JSONObject response = new JSONObject(clientResponse.getEntity(String.class));
            JSONArray resultArray = response.getJSONArray("data");
            for (int i = 0; i < resultArray.length(); i++)
            {
                readings.add(new TimestampedDouble(resultArray.getJSONObject(i).getDouble("reading"),
                        resultArray.getJSONObject(i).getLong("timestamp")));
            }
            return readings;
        }
    }

    public DataServiceReadings refreshMetricFromDB(DataServiceMeter meter, DataServiceMetric metric)
    {
        readings = new DataServiceReadings();
        clientResponse = getResource(DEVICE+meter.getTag()+"/"+metric.getTag()+"/")
                .get(ClientResponse.class);
        lastRestError = clientResponse.getStatus();
        if (lastRestError != REST_REQUEST_SUCCESSFUL)
        {
            printLastError();
            return readings;
        }
        else
        {
            readings.setMeter(meter);
            readings.setMetric(metric);
            JSONObject response = new JSONObject(clientResponse.getEntity(String.class));
            JSONArray resultArray = response.getJSONArray("data");
            TimestampedDouble reading;
            for (int i = 0; i < resultArray.length(); i++)
            {
                reading = new TimestampedDouble(resultArray.getJSONObject(i).getDouble("reading"),
                        resultArray.getJSONObject(i).getLong("timestamp"));
                readings.add(reading);
                if (i==0) readings.setStartTime(reading.getTimestamp());
                readings.setEndTime(reading.getTimestamp());
            }
            return readings;
        }
    }

    private void printLastError()
    {
        if (lastRestError == REST_REQUEST_SUCCESSFUL)
        {
            System.out.println("REST request was successful");
        }
        {
            System.out.println("Failed : HTTP error code : "
                    + clientResponse.getStatus() + " "
                    + clientResponse.getEntity(String.class));
        }
    }
    public Collection<DataServiceMeter> getMeters()
    {
        return meters;
    }
    public Collection<DataServiceMetric> getMetrics()
    {
        return metrics;
    }
}
