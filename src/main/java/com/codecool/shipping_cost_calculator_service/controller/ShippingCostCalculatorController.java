package com.codecool.shipping_cost_calculator_service.controller;

import com.codecool.shipping_cost_calculator_service.model.ShippingOption;
import com.codecool.shipping_cost_calculator_service.service.GoogleMapsAPIService;
import org.json.JSONObject;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created by shevah on 10/01/17.
 */
public class ShippingCostCalculatorController {

    private final GoogleMapsAPIService apiService;

    public ShippingCostCalculatorController(GoogleMapsAPIService apiService) {
        this.apiService = apiService;
    }

    public String status(Request request, Response response) {
        return "ok";
    }

    public JSONObject generateOptions(Request request, Response response) throws IOException, URISyntaxException {
        String originAddress = request.queryParams("origin");
        String destinationAddress = request.queryParams("destination");

        if (originAddress.trim().isEmpty() || destinationAddress.trim().isEmpty()) {
            return apiService.generateErrorJson(response, 400, "Origin or destination cannot be empty or whitespaces only");
        }

        String rawData = apiService.requestData(originAddress, destinationAddress);
        JSONObject extractedData = apiService.extractData(response, rawData);

        if (extractedData.has("error")) {
            return extractedData;
        }

        JSONObject allOptionsJSON = new JSONObject();
        for (String type : ShippingOption.TYPES) {
            allOptionsJSON.put(type, (new JSONObject(new ShippingOption(type, extractedData.getString("originFound"),
                    extractedData.getString("destinationFound"), extractedData.getInt("dist"),
                    extractedData.getInt("time")))));
        }
        return allOptionsJSON;
    }


}