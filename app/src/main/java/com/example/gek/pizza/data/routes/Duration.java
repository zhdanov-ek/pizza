package com.example.gek.pizza.data.routes;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Ivleshch on 27.01.2017.
 */

public class Duration {
    @SerializedName("text")
    @Expose
    private String text;
    @SerializedName("value")
    @Expose
    private Integer value;

    /**
     *
     * @return
     * The text
     */
    public String getText() {
        return text;
    }

    /**
     *
     * @param text
     * The text
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     *
     * @return
     * The value
     */
    public Integer getValue() {
        return value;
    }

    /**
     *
     * @param value
     * The value
     */
    public void setValue(Integer value) {
        this.value = value;
    }
}
