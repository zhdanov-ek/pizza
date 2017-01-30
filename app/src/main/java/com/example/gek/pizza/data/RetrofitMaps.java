package com.example.gek.pizza.data;

import com.example.gek.pizza.data.routes.Example;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Ivleshch on 27.01.2017.
 */

public interface RetrofitMaps {

    @GET("api/directions/json?key=AIzaSyBGRee53WUzcJ-MQGs3kVO3DsOMIZAfZXI")
    Call<Example> getDistanceDuration(@Query("units") String units, @Query("origin") String origin, @Query("destination") String destination, @Query("mode") String mode);

}
