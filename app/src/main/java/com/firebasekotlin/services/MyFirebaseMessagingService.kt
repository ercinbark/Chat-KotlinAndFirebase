package com.firebasekotlin.services

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(p0: RemoteMessage?) {

        var bildirimBaslik=p0?.notification?.title
        var bildirimBody=p0?.notification?.body
        var data=p0?.data

        Log.e("FCM","BildirimBaslik : $bildirimBaslik")
        Log.e("FCM","BildirimBody: $bildirimBody")
        Log.e("FCM","Data : $data")
    }
}