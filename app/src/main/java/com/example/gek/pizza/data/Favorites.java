package com.example.gek.pizza.data;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.example.gek.pizza.data.Const.db;

/**
 * Singleton store favorite dishesFavorites for current user
 */

public class Favorites {
    private static Favorites instance;
    private ArrayList<Dish> dishesFavorites;
    private String userId;
    public static Favorites getInstance() {
        if (instance == null) {
            instance = new Favorites();
        }
        return instance;
    }

    private Favorites() {
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        dishesFavorites = new ArrayList<>();
        db.child(Const.CHILD_USERS)
                .child(userId)
                .child(Const.CHILD_USER_FAVORITES)
                .addValueEventListener(listenerFavoritesDishes);
    }

    private ValueEventListener listenerFavoritesDishes = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            dishesFavorites.clear();
            for (DataSnapshot snapshotChild: dataSnapshot.getChildren()) {
                dishesFavorites.add(snapshotChild.getValue(Dish.class));
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    };

    /** Add new dish in favorites if it not find in list */
    public void addDish(Dish dish){
        if (! searchDish(dish)){
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            db.child(Const.CHILD_USERS)
                    .child(userId)
                    .child(Const.CHILD_USER_FAVORITES)
                    .child(dish.getKey())
                    .setValue(dish);
        }
    }

    
    /** Find dish in list*/
    public Boolean searchDish(Dish dishSearch){
        for (Dish dish: dishesFavorites) {
            if (dish.getKey().contentEquals(dishSearch.getKey())){
                return true;
            }
        }
        return false;
    }

    /** Remove dish from favorites */
    public void removeDish(Dish dish) {
        String keyRemove = dish.getKey();
        db.child(Const.CHILD_USERS)
            .child(userId)
            .child(Const.CHILD_USER_FAVORITES)
            .child(keyRemove)
            .removeValue();
    }

}
