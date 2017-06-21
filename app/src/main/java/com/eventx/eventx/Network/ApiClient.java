package com.eventx.eventx.Network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Nishant on 5/10/2017.
 */

public class ApiClient {

    static ApiInterface apiInterface;
    public static ApiInterface getApiInterface(){
        if(apiInterface==null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://developer.eventshigh.com/events/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            apiInterface = retrofit.create(ApiInterface.class);
        }
        return apiInterface;

    }
}
