package com.eventx.eventx.Network;

import com.eventx.eventx.Model.EventModel;
import com.eventx.eventx.Model.Result;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by Nishant on 5/10/2017.
 */

public interface ApiInterface {

    @GET("delhi?key=ev3nt5h1ghte5tK3y")
    Call<Result> getDelhiEvents();
}
