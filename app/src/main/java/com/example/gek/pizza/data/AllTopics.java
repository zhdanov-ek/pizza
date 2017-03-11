package com.example.gek.pizza.data;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;

import static com.example.gek.pizza.data.Const.db;


 /**
 * Синглтон, который хранит актуальный список всех топиков для рассылки. Загружается при старте программы.
 * Данные используются отправки уведомлений
 */


public class AllTopics {

    public ArrayList<String> topics;
    private static AllTopics instance;

    private AllTopics(){
        topics = new ArrayList<>();
        db.child(Const.CHILD_TOPICS).addValueEventListener(topicsListener);
    }

    public static synchronized AllTopics getInstance(){
        if (instance == null) {
            instance = new AllTopics();
        }
        return instance;
    }

    private ValueEventListener topicsListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            topics.clear();
            for (DataSnapshot childSnapShot : dataSnapshot.getChildren()) {
                for (DataSnapshot child: dataSnapshot.getChildren()) {
                    if(!topics.contains(child.getValue().toString())){
                        topics.add(child.getValue().toString());
                        FirebaseMessaging.getInstance().subscribeToTopic(child.getValue().toString());
                    }

                }
            }

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    };
}
