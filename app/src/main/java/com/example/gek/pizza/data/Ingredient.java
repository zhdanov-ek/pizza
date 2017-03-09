package com.example.gek.pizza.data;

/**
 * Ingredient for pizza
 */

public class Ingredient {
    private String name;
    private int listImageResource;
    private int pizzaImageResource;
    private float price;


    public Ingredient() {
    }

    public Ingredient(String name, float price, int listImageResource, int pizzaImageResource) {
        this.name = name;
        this.listImageResource = listImageResource;
        this.pizzaImageResource = pizzaImageResource;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getListImageResource() {
        return listImageResource;
    }

    public void setListImageResource(int listImageResource) {
        this.listImageResource = listImageResource;
    }

    public int getPizzaImageResource() {
        return pizzaImageResource;
    }

    public void setPizzaImageResource(int pizzaImageResource) {
        this.pizzaImageResource = pizzaImageResource;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

}
