package com.example.gek.pizza.data.routes;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ivleshch on 27.01.2017.
 */

public class Example {
    @SerializedName("routes")
    @Expose
    private List<Route> routes = new ArrayList<>();

    /**
     *
     * @return
     * The routes
     */
    public List<Route> getRoutes() {
        return routes;
    }

    /**
     *
     * @param routes
     * The routes
     */
    public void setRoutes(List<Route> routes) {
        this.routes = routes;
    }
}
