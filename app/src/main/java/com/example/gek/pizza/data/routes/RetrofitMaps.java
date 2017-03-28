package com.example.gek.pizza.data.routes;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * interface for google routes
 */

public interface RetrofitMaps {

    @GET("api/directions/json?key=AIzaSyBGRee53WUzcJ-MQGs3kVO3DsOMIZAfZXI")
    Call<Example> getDistanceDuration(@Query("units") String units, @Query("origin") String origin, @Query("destination") String destination, @Query("mode") String mode);

}
