package com.example.gek.pizza.services;

import com.example.gek.pizza.data.Connection;
import com.example.gek.pizza.data.Const;
import com.example.gek.pizza.helpers.Utils;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Сервис который отлавливает изменения токена. Используется для нотификаций
 */

public class InstanceIDListenerService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        if(Connection.getInstance().getCurrentAuthStatus()!= Const.AUTH_SHOP){
            Utils.subscribeOrUnsubscribeFromTopic(true);
        } else {
            Utils.subscribeOrUnsubscribeFromTopic(false);
        }
    }

}

