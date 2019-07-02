package com.firebasekotlin.services

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService

class MyFirebaseInstanceIDService :FirebaseInstanceIdService() {

    override fun onTokenRefresh() {
        var refreshedToken:String?=FirebaseInstanceId.getInstance().token

        tokenVeriTabanianaKaydet(refreshedToken)
    }

    private fun tokenVeriTabanianaKaydet(refreshedToken: String?) {
        var ref=FirebaseDatabase.getInstance().reference
            .child("kullanici")
            .child(FirebaseAuth.getInstance().currentUser?.uid)
            .child("mesaj_token")
            .setValue(refreshedToken)
    }
}