package com.example.gek.pizza.data;

/**
 * Store dish and count
 */

public class Order {
    private Dish dish;
    private int count;

    public Dish getDish() {
        return dish;
    }

    public void setDish(Dish dish) {
        this.dish = dish;
    }

    public Order() {
    }

    public Order(Dish dish) {
        this.dish = dish;
        this.count = 1;
    }

    public float getSum(){
        return dish.getPrice() * count;
    }

    public void setCount(int count){
        this.count = count;
    }

    public int getCount(){
        return count;
    }

    public String getNameDish(){
        return dish.getName();
    }

    public String getPhotoUrlDish(){
        return dish.getPhotoUrl();
    }

    public float getPriceDish(){
        return dish.getPrice();
    }

    public String getKeyDish(){
        return dish.getKey();
    }

}
