package org.ladbury.dataServicePkg;

import java.time.Instant;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.json.JSONArray;
import org.ladbury.meterPkg.TimestampedDouble;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class DataService // copied from DBRestAPI in MQTTListener
{
    public static final String DEFAULT_API_URL = "http://192.168.1.127:3000/api/";
    public static final int REST_REQUEST_SUCCESSFUL = 200;

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
        System.out.println(resource);
        if(!resources.containsKey(resource))
            resources.put(resource, restClient.resource(this.apiUrl + resource));
        return resources.get(resource);
    }

    public Collection<DataServiceMeter> getAvailableMeters()
    {
        clientResponse = getResource("location").get(ClientResponse.class);
        lastRestError = clientResponse.getStatus();
        if (lastRestError != REST_REQUEST_SUCCESSFUL)
        {
            printLastError();
            return new ArrayList<>();
        }

        else
        {
            JSONArray data = new JSONArray(clientResponse.getEntity(String.class));
            List <DataServiceMeter> results = new ArrayList<>();
            for (int i = 0; i < data.length(); i++)
            {
                results.add(new DataServiceMeter(
                        data.getJSONObject(i).getString("name"),
                        data.getJSONObject(i).getString("tag"),
                        data.getJSONObject(i).getString("description")));
            }
            return results;
        }
    }

    public Collection<String> getAvailableMeterNames()
    {
        clientResponse = getResource("location").get(ClientResponse.class);
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
                results.add(data.getJSONObject(i).getString("name"));
            }
            return results;
        }
    }

    public Collection<DataServiceMetric> getAvailableMetrics()
    {
        clientResponse = getResource("datatype").get(ClientResponse.class);
        lastRestError = clientResponse.getStatus();
        if (lastRestError != REST_REQUEST_SUCCESSFUL)
        {
            printLastError();
            return new ArrayList<>();
        }

        else
        {
            JSONArray data = new JSONArray(clientResponse.getEntity(String.class));
            List <DataServiceMetric> results = new ArrayList<>();
            for (int i = 0; i < data.length(); i++)
            {
                results.add(new DataServiceMetric(
                        data.getJSONObject(i).getString("name"),
                        data.getJSONObject(i).getString("tag"),
                        data.getJSONObject(i).getString("symbol"),
                        data.getJSONObject(i).getString("description")));
            }
            return results;
        }
    }
    public Collection<String> getAvailableMetricNames()
    {
        clientResponse = getResource("datatype").get(ClientResponse.class);
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
                results.add(data.getJSONObject(i).getString("name"));
            }
            return results;
        }
    }

    public Instant getEarliestMetric(String meterName, String metricName)
    {
        return Instant.now();
    }
    public Instant getLatestMetric(String meterName, String metricName)
    {
        return Instant.now();
    }

    public Collection<TimestampedDouble> getDBResourceForPeriod(String resource, String start, String end)
    {

        clientResponse = getResource(resource)
                .queryParam("start", start)
                .queryParam("end", end)
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
            List <TimestampedDouble> results = new ArrayList<>();
            for (int i = 0; i < data.length(); i++)
            {
                results.add(new TimestampedDouble(data.getJSONObject(i).getDouble("reading"),
                        data.getJSONObject(i).getString("timestamp")));
            }
            return results;
        }
    }

    public void printDBResourceForPeriod(String resource, String start, String end)
    {
        for(TimestampedDouble reading: getDBResourceForPeriod(resource,start,end))
        {
            System.out.println(reading);
        }
    }

    public Collection<String> getDBResourceForPeriodAsStrings(String resource, String start, String end)
    {
        List <String> results = new ArrayList<>();
        for(TimestampedDouble reading: getDBResourceForPeriod(resource,start,end))
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
