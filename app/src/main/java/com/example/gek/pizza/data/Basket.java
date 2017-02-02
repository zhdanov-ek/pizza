package com.example.gek.pizza.data;

import java.util.ArrayList;

/**
 * Синглтон - корзина, куда добавляются и где хранятся все заказы
 */

public class Basket {
    public ArrayList<Order> orders;
    private String numberDelivery;
    private static Basket instance;


    private Basket() {
        orders = new ArrayList<>();
        numberDelivery = "";
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

    // после отправки заявки на доставку в этот метод подаем номер заявки и очищаем корзину
    public void makeDelivery(String numberDelivery){
        // todo по умному номер нужно куда-нибудь сохранить на случай когда апп перезапустят
        // по этому номер потом будем отслеживать состояние выполнения заявки
        this.numberDelivery = numberDelivery;
        orders.clear();
    }



    public String getNumberDelivery() {
        return numberDelivery;
    }

    public void setNumberDelivery(String numberDelivery) {
        this.numberDelivery = numberDelivery;
    }

}
