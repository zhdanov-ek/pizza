package com.example.gek.pizza.data;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static com.example.gek.pizza.data.Const.db;

/**
 * Singleton store favorite dishes for current user
 */

public class Favorites {
    private static Favorites instance;
    private ArrayList<FavoriteDish> dishesLinks;
    private String userId;
    public static Favorites getInstance() {
        if (instance == null) {
            instance = new Favorites();
        }
        return instance;
    }

    private Favorites() {
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        dishesLinks = new ArrayList<>();
        db.child(Const.CHILD_USERS)
                .child(userId)
                .child(Const.CHILD_USER_FAVORITES)
                .addValueEventListener(listenerFavoritesDishes);
    }


    /** Make list of dishes for correctly add and remove items */
    private ValueEventListener listenerFavoritesDishes = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            dishesLinks.clear();
            FavoriteDish favoriteDish;
            for (DataSnapshot snapshotChild: dataSnapshot.getChildren()) {
                favoriteDish = snapshotChild.getValue(FavoriteDish.class);
                dishesLinks.add(favoriteDish);
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    };

    /** Add new dish in favorites if it not find in list */
    public void addDish(String keyDish){
        if (searchDish(keyDish) == null){
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            String key = db.child(Const.CHILD_USERS)
                    .child(userId)
                    .child(Const.CHILD_USER_FAVORITES)
                    .push()
                    .getKey();
            db.child(Const.CHILD_USERS)
                    .child(userId)
                    .child(Const.CHILD_USER_FAVORITES)
                    .child(key)
                    .setValue(new FavoriteDish(keyDish, key));
        }
    }


    /** Return key of favoriteDish if result true and empty if fail */
    public String searchDish(String keyDish){
        for (FavoriteDish favoriteDish: dishesLinks) {
            if (favoriteDish.getKeyOfDish().contentEquals(keyDish)){
                return favoriteDish.getKey();
            }
        }
        return null;
    }

    public void removeDish(String keyDish) {
        String keyRemove = searchDish(keyDish);
        if (keyRemove != null) {
            db.child(Const.CHILD_USERS)
                    .child(userId)
                    .child(Const.CHILD_USER_FAVORITES)
                    .child(keyRemove)
                    .removeValue();
        }
    }

}