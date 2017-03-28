package com.example.gek.pizza.data;

/**
 * Favorites
 */

public class FavoriteDish {
    public FavoriteDish() {
    }

    public FavoriteDish(String keyOfDish, String key) {
        this.keyOfDish = keyOfDish;
        this.key = key;
    }

    private String keyOfDish;
    private String key;

    public String getKeyOfDish() {
        return keyOfDish;
    }

    public void setKeyOfDish(String keyOfDish) {
        this.keyOfDish = keyOfDish;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }


}