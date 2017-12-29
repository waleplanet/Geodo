package com.app.waleent.geodo;

/**
 * Created by wale on 10/31/17.
 */

class AutoCompletePlace {

    String id;
    String description;
    double latitude;
    double longitude;

    public AutoCompletePlace(String placeId, String desc, double lat, double lon) {
        id= placeId;
        description = desc;
        latitude =lat;
        longitude = lon;
    }

    @Override
    public String toString() {
        return description;
    }
}
