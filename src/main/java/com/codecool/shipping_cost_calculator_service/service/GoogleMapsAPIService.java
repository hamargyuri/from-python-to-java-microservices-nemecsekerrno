package com.codecool.shipping_cost_calculator_service.service;

import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONObject;
import spark.Response;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

/**
 * Created by shevah on 10/01/17.
 */
public class GoogleMapsAPIService {
    private static final String GoogleAPIURL= "https://maps.googleapis.com/maps/api/distancematrix/json";
    private static final String GoogleAPIKey = "AIzaSyBLun5iVfffgdRpSLtqmbsyYMrBRYfpG78";

    private static GoogleMapsAPIService INSTANCE;

    public static GoogleMapsAPIService getINSTANCE() {
        if (INSTANCE == null) {
            INSTANCE = new GoogleMapsAPIService();
        }
        return INSTANCE;
    }

    public String requestData(String origin, String destination) throws IOException, URISyntaxException {
        URIBuilder builder = new URIBuilder(GoogleAPIURL).addParameter("origins", origin)
                .addParameter("destinations", destination).addParameter("key", GoogleAPIKey);
        return execute(builder.build());
    }

    private String execute(URI uri) throws IOException {
        return Request.Get(uri)
                .execute()
                .returnContent()
                .asString();
    }

    public JSONObject extractData(Response res, String rawData) {
        JSONObject rawDataJSON = new JSONObject(rawData);
        String status = rawDataJSON.getString("status");
        JSONObject elements;
        String elementsStatus;

        switch (status){
            case "OVER_QUERY_LIMIT":
                return generateErrorJson(res, 429,
                        "The application has requested too many elements within the allowed time period." +
                                "The request should succeed if you try again after a reasonable amount of time.");
            case "REQUEST_DENIED":
                return generateErrorJson(res, 500,
                        "The service denied use of the Distance Matrix service by your web page.");
            case "UNKNOWN_ERROR":
                return generateErrorJson(res, 500,
                        "A Distance Matrix request could not be processed due to a server error." +
                                "The request may succeed if you try again.");
            case "OK":
                elements = rawDataJSON.getJSONArray("rows").getJSONObject(0).getJSONArray("elements").getJSONObject(0);
                elementsStatus = elements.getString("status");
                break;
            default:
                return generateErrorJson(res, 501, "Distance Matrix status message not implemented");
        }

        switch (elementsStatus) {
            case "NOT_FOUND":
                return generateErrorJson(res, 400,
                        "The origin and/or destination of this pairing could not be geocoded.");
            case "ZERO_RESULTS":
                return generateErrorJson(res, 400, "No route could be found between the origin and destination.");
            case "OK":
                Integer rawDistance = elements.getJSONObject("distance").getInt("value");
                Integer rawTime = elements.getJSONObject("duration").getInt("value");
                HashMap<String, Object> optionValues = new HashMap<>();
                optionValues.put("dist", rawDistance/1000.f);
                optionValues.put("time", rawTime/3600.f);
                optionValues.put("originFound", rawDataJSON.getJSONArray("origin_addresses").get(0));
                optionValues.put("destinationFound", rawDataJSON.getJSONArray("destination_addresses").get(0));
                return new JSONObject(optionValues);
            default:
                return generateErrorJson(res, 501, "Distance Matrix elements status message not implemented");
        }
    }


    public JSONObject generateErrorJson(Response response, int statusCode, String message) {
        response.status(statusCode);
        response.type("application/json");
        JSONObject errorJson = new JSONObject();
        errorJson.put("error", message);
        return errorJson;
    }

}
