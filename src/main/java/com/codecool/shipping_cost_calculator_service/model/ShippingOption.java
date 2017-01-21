package com.codecool.shipping_cost_calculator_service.model;

import java.util.Arrays;
import java.util.Currency;
import java.util.List;

public class ShippingOption {
    public static final List<String> TYPES = Arrays.asList("truck", "truckViaHighway", "expressCourier",
            "timeMachine");
    private String originFound;
    private String destinationFound;
    private int distanceInKm;
    private int timeInHours;
    private String details;
    private float cost;
    private final Currency currency = Currency.getInstance("USD");

//    ------ PRICING CONSTANTS ------
    private final float StandardFeePerKm = 0.01f;
    private final float HighwayDistanceModifier = 0.88f;
    private final float HighwayTimeModifier = 0.6f;
    private final float HighwayFeePerKm = 0.02f;
    private final int ExpressCourierTime = distanceInKm < 400 ? 48 : 96;
    private final int ExpressCourierCost = distanceInKm < 400 ? 5 : 10;
    private final int TimeMachineTime = 1;
    private final int TimeMachineCost = 6000000;
//    -------------------------------


    /**
     * ShippingOption constructor.
     * <p>
     *     It generates all (4) options
     *     based on the identifiers
     *     and the data it receives
     *     from Google Maps Distance Matrix.
     * </p>
     * @param type Shipping method identifier.
     * @param originFound The exact origin address found by GMDM.
     * @param destinationFound The exact destination address found by GMDM.
     * @param distanceInKm
     * @param timeInHours
     */
    public ShippingOption(String type, String originFound, String destinationFound, float distanceInKm, float timeInHours) {
        this.originFound = originFound;
        this.destinationFound = destinationFound;
        int roundedTimeInHours = (int) Math.ceil(timeInHours) < 1 ? 1 : (int) Math.ceil(timeInHours);
        int roundedDistanceInKm = (int) Math.ceil(distanceInKm) < 1 ? 1 : (int) Math.ceil(distanceInKm);

        switch (type) {
            case "truck":
                this.distanceInKm = roundedDistanceInKm;
                this.timeInHours = roundedTimeInHours;
                this.details = "Standard truck avoiding highways";
                this.cost = (int) Math.ceil(roundedDistanceInKm * StandardFeePerKm);
                break;
            case "truckViaHighway":
                this.distanceInKm = (int) Math.ceil(roundedDistanceInKm * HighwayDistanceModifier);
                this.timeInHours = (int) Math.ceil(roundedTimeInHours * HighwayTimeModifier);
                this.details = "Standard truck via highway";
                this.cost = (int) Math.ceil(roundedDistanceInKm * HighwayFeePerKm);
                break;
            case "expressCourier":
                this.distanceInKm = roundedDistanceInKm;
                this.timeInHours = ExpressCourierTime;
                this.details = "Express courier with fixed cost and time";
                this.cost = ExpressCourierCost;
                break;
            case "timeMachine":
                this.distanceInKm = roundedDistanceInKm;
                this.timeInHours = TimeMachineTime;
                this.details = "Most advanced technology, totally safe - we promise";
                this.cost = TimeMachineCost;
                break;
            default:
                this.distanceInKm = roundedDistanceInKm;
                this.timeInHours = roundedTimeInHours;
                this.details = "Standard truck avoiding highways";
                this.cost = (int) Math.ceil(roundedDistanceInKm * StandardFeePerKm);
                break;
        }
    }

    public String getOriginFound() {
        return originFound;
    }

    public String getDestinationFound() {
        return destinationFound;
    }

    public float getDistanceInKm() {
        return distanceInKm;
    }

    public float getTimeInHours() {
        return timeInHours;
    }

    public String getDetails() {
        return details;
    }

    public float getCost() {
        return cost;
    }

    public Currency getCurrency() {
        return currency;
    }
}
