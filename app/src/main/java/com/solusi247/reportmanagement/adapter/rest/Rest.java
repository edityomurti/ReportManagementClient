package com.solusi247.reportmanagement.adapter.rest;

import retrofit.RestAdapter;

/**
 * Created by usernames on 22/06/16.
 */
public class Rest {
//    private static final String URL = "http://192.168.1.72:8081/projectManagementApi/rest"; //Laptop Edit
    private static final String URL = "http://192.168.1.228:8080/reportManagementApi/rest"; //server solusi
    private RestAdapter restAdapter;
    private RestApi restApi;

    public Rest() {
        restAdapter = new RestAdapter.Builder().setEndpoint(URL).setLogLevel(RestAdapter.LogLevel.FULL).build();
        restApi = restAdapter.create(RestApi.class);
    }

    public RestApi getRestApi(){
        return restApi;
    }
}
