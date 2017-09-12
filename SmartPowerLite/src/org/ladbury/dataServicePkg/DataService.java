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
    private static final String DEVICE = "device/"; //for specfic device commands
    private static final String DATA_TYPES = "datatypes/"; //for list of dataTypes
    private static final int    REST_REQUEST_SUCCESSFUL = 200;

    private final HashMap<String, WebResource> resources;
    private final Client restClient;
    private int lastRestError;
    private ClientResponse clientResponse;
    private final String apiUrl;

    public DataService(String apiUrl)
    {
        resources = new HashMap<>();
        clientResponse = null;
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

    public Collection<DataServiceMeter> getAvailableMeters()
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
            List<DataServiceMeter> results = new ArrayList<>();
            for (int i = 0; i < resultArray.length(); i++)
            {
                results.add(new DataServiceMeter(
                        resultArray.getJSONObject(i).getString("name"),
                        resultArray.getJSONObject(i).getString("tag"),
                        resultArray.getJSONObject(i).getString("description")));
            }
            return results;
        }
    }

    public Collection<String> getAvailableMeterNames()
    {
        Collection<DataServiceMeter> meterResults = getAvailableMeters();
        List<String> results = new ArrayList<>();
        for (DataServiceMeter meter:meterResults )
        {
            results.add(meter.getDisplayName());
        }
        return results;
    }

    public Collection<DataServiceMetric> getAvailableMetrics()
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
            List <DataServiceMetric> results = new ArrayList<>();
            for (int i = 0; i < resultArray.length(); i++)
            {
                results.add(new DataServiceMetric(
                        resultArray.getJSONObject(i).getString("name"),
                        resultArray.getJSONObject(i).getString("tag"),
                        resultArray.getJSONObject(i).getString("symbol"),
                        resultArray.getJSONObject(i).getString("description")));
            }
            return results;
        }
    }
    public Collection<String> getAvailableMetricNames()
    {
        Collection<DataServiceMetric> metricResults = getAvailableMetrics();
        List<String> results = new ArrayList<>();
        for (DataServiceMetric metric:metricResults )
        {
            results.add(metric.getDisplayName());
        }
        return results;
    }

    public Instant getEarliestMetric(String meterName, String metricName)
    {
        return Instant.now();
    }
    public Instant getLatestMetric(String meterName, String metricName)
    {
        return Instant.now();
    }

    public Collection<TimestampedDouble> getDBResourceForPeriod(DataServiceMeter meter, DataServiceMetric metric, Instant start, Instant end)
    {

        clientResponse = getResource(DEVICE+meter.getTag()+"/"+metric.getTag()+"/")
                .queryParam("start", ((Long) start.toEpochMilli()).toString())
                .queryParam("end", ((Long) end.toEpochMilli()).toString())
                .get(ClientResponse.class);
        lastRestError = clientResponse.getStatus();
        if (lastRestError != REST_REQUEST_SUCCESSFUL)
        {
            printLastError();
            return new ArrayList<>();
        }
        else
        {
            JSONObject response = new JSONObject(clientResponse.getEntity(String.class));
            JSONArray resultArray = response.getJSONArray("data");;
            List <TimestampedDouble> results = new ArrayList<>();
            for (int i = 0; i < resultArray.length(); i++)
            {
                results.add(new TimestampedDouble(resultArray.getJSONObject(i).getDouble("reading"),
                        resultArray.getJSONObject(i).getLong("timestamp")));
            }
            return results;
        }
    }

    public void printDBResourceForPeriod(DataServiceMeter meter,DataServiceMetric metric, Instant start, Instant end)
    {
        for(TimestampedDouble reading: getDBResourceForPeriod(meter,metric,start,end))
        {
            System.out.println(reading);
        }
    }

    public Collection<String> getDBResourceForPeriodAsStrings(DataServiceMeter meter,DataServiceMetric metric, Instant start, Instant end)
    {
        List <String> results = new ArrayList<>();
        for(TimestampedDouble reading: getDBResourceForPeriod(meter,metric,start,end))
        {
            results.add(reading.toString());
        }
        return results;
    }

    public Collection<String> getDBResourceAsStrings(String resource)
    {

        clientResponse = getResource(resource)
                 .get(ClientResponse.class);
        lastRestError = clientResponse.getStatus();
        if (lastRestError != REST_REQUEST_SUCCESSFUL)
        {
            printLastError();
            return new ArrayList<>();
        }
        else
        {
            JSONArray data = new JSONArray(clientResponse.getEntity(String.class));
            List <String> results = new ArrayList<>();
            for (int i = 0; i < data.length(); i++)
            {
                results.add(data.getJSONObject(i).getDouble("reading") + " " +
                        data.getJSONObject(i).getString("timestamp"));
            }
            return results;
        }
    }

    public void printLastError()
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

}
