package com.example.gek.pizza.helpers;

import com.example.gek.pizza.data.Const;
import com.example.gek.pizza.data.Dish;
import com.example.gek.pizza.data.Order;

import java.util.ArrayList;

/**
 * Синглтон - корзина, куда добавляются и где хранятся все заказы
 *
 * Своя собранная пицца формируется в модель Dish на лету и не хранится в общем списке блюд как шаблон
 * В заказе на доставку она храниится в отдельном массиве
 */

public class Basket {
    public ArrayList<Order> orders;
    private static Basket instance;
    private ArrayList<String> textMyPizza;      // Списки ингрединтов кастомных пицц

    private ArrayList<Integer> numbersMyPizza;  // Количество каждой кастомной пиццы


    private Basket() {
        orders = new ArrayList<>();
        textMyPizza = new ArrayList<>();
        numbersMyPizza = new ArrayList<>();
    }

    // Получаем инстанс через метод, а не конструктор, который скрыт
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

    // возвращает итоговую сумму по всем заказам
    public float getTotalSum(){
        float sum = 0;
        for (Order order: orders) {
            sum = sum + order.getSum();
        }
        return sum;
    }

    // Возвращает количество блюд
    public ArrayList<Integer> getNumberDishes(){
        ArrayList<Integer> list = new ArrayList<>();
        for (Order order: orders) {
            list.add(order.getCount());
        }
        return list;
    }

    // Возвращает список ключей блюд
    public ArrayList<String> getKeysDishes(){
        ArrayList<String> list = new ArrayList<>();
        for (Order order: orders) {
            list.add(order.getDish().getKey());
        }
        return list;
    }

    // Изымаем кастомные пиццы из общего списка заказов и помещаем их в отдельные массивы
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
