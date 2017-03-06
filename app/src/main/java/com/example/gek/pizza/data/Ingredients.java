package com.example.gek.pizza.data;

import com.example.gek.pizza.R;

import java.util.ArrayList;

/**
 * Create list with ingredients for pizza
 */

public class Ingredients {

    public static ArrayList<Ingredient> getIngredients(){
        ArrayList<Ingredient> list = new ArrayList<>();
        list.add(new Ingredient("Tomato", 20, R.drawable.pizza_list_tomato, R.drawable.pizza_tomatoes));
        list.add(new Ingredient("Ananas", 23, R.drawable.pizza_list_ananas, R.drawable.pizza_ananas));
        list.add(new Ingredient("Pepper", 15, R.drawable.pizza_list_pepper, R.drawable.pizza_pepper));
        list.add(new Ingredient("Olives", 26, R.drawable.pizza_list_olives, R.drawable.pizza_olives));
        list.add(new Ingredient("Mushrooms canned", 24, R.drawable.pizza_list_mushrooms_canned, R.drawable.pizza_mushroom_canned));
        list.add(new Ingredient("Mushrooms fried", 32, R.drawable.pizza_list_mushrooms_fried, R.drawable.pizza_mushrooms_fried));
        list.add(new Ingredient("Sausage", 30, R.drawable.pizza_list_sausage, R.drawable.pizza_sausage));
        list.add(new Ingredient("Bacon", 31, R.drawable.pizza_list_bacon, R.drawable.pizza_bacon));

        return list;
    }

    public static Ingredient getBasis(){
        return new Ingredient("Basis and cheese", 40, 0, 0);
    }
}
