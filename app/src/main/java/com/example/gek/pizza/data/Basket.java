package com.example.gek.pizza.data;

import java.util.ArrayList;

/**
 * Синглтон - корзина, куда добавляются и где хранятся все заказы
 */

public class Basket {
    public ArrayList<Order> orders;
    private static Basket instance;

    private Basket() {
        orders = new ArrayList<>();
    }

    // Получаем инстанс через метод, а не конструктор, который скрыт
    public static synchronized Basket getInstance(){
        if (instance == null) {
            instance = new Basket();
        }
        return instance;
    }

    public void addDish(Dish dish){
        orders.add(new Order(dish));
    }

    // Устанавливаем новое кол-во в заказе (увеличиваем "2"/уменьшаем "-2"/удаляем "0")
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

}
