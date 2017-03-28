package com.example.gek.pizza.helpers;

import com.example.gek.pizza.data.Const;
import com.example.gek.pizza.data.Dish;
import com.example.gek.pizza.data.Order;

import java.util.ArrayList;

/**
 * Basket store all orders of current user
 * Custom pizza saved in model DISH (make in code)
 * In Delivery custom pizza store in other list
 */

public class Basket {
    public ArrayList<Order> orders;
    private static Basket instance;
    private ArrayList<String> textMyPizza;      // List of ingredients custom pizza
    private ArrayList<Integer> numbersMyPizza;  // Quantity of each custom pizza

    private Basket() {
        orders = new ArrayList<>();
        textMyPizza = new ArrayList<>();
        numbersMyPizza = new ArrayList<>();
    }

    // get instance from method. Constructor use in method
    public static synchronized Basket getInstance(){
        if (instance == null) {
            instance = new Basket();
        }
        return instance;
    }

    public void clearOrders(){
        orders.clear();
        textMyPizza.clear();
        numbersMyPizza.clear();
    }


    public void addDish(Dish dish){
        orders.add(new Order(dish));
    }

    // Change quantity in order (increase "2"/ reduce "-2"/ remove "0")
    public void changeCount(String dishKey, int count){
        int i = 0;
        while (orders.size() > i){
            if (orders.get(i).getKeyDish().contentEquals(dishKey)){
                if (count == 0) {
                    orders.remove(i);
                } else {
                    orders.get(i).setCount(count);
                }
            }
            i++;
        }
    }

    public float getTotalSum(){
        float sum = 0;
        for (Order order: orders) {
            sum = sum + order.getSum();
        }
        return sum;
    }

    public ArrayList<Integer> getNumberDishes(){
        ArrayList<Integer> list = new ArrayList<>();
        for (Order order: orders) {
            list.add(order.getCount());
        }
        return list;
    }

    public ArrayList<String> getKeysDishes(){
        ArrayList<String> list = new ArrayList<>();
        for (Order order: orders) {
            list.add(order.getDish().getKey());
        }
        return list;
    }


    // need for correctly show in lists. Saved in Delivery in other keys
    public Boolean extractMyPizza(){
        Boolean isFinded  = false;
        textMyPizza = new ArrayList<>();
        numbersMyPizza = new ArrayList<>();
        for (int i = 0; i < orders.size(); i++) {
            Order order = orders.get(i);
            if (order.getKeyDish().contentEquals(Const.KEY_DISH_MY_PIZZA)){
                textMyPizza.add(order.getDish().getDescription());
                numbersMyPizza.add(order.getCount());
                orders.remove(i);
                isFinded = true;
            }
        }
        return isFinded;
    }

    public ArrayList<String> getTextMyPizza() {
        return textMyPizza;
    }

    public ArrayList<Integer> getNumbersMyPizza() {
        return numbersMyPizza;
    }


}
