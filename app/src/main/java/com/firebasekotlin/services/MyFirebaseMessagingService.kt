package com.firebasekotlin.services

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Ringtone
import android.media.RingtoneManager
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.firebasekotlin.MainActivity
import com.firebasekotlin.R
import com.firebasekotlin.SohbetOdasiActivity
import com.firebasekotlin.model.SohbetOdasi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    var okunmayiBeklenenMesajSayisi = 0

    override fun onMessageReceived(p0: RemoteMessage?) {


        if (!activityKontrolEt()) {
            var bildirimBaslik = p0?.notification?.title
            var bildirimBody = p0?.notification?.body
            var data = p0?.data


            var baslik = p0?.data?.get("baslik")
            var icerik = p0?.data?.get("icerik")
            var bildirim_turu = p0?.data?.get("bildirim_turu")
            var sohbet_odasi_id = p0?.data?.get("sohbet_odasi_id")

            Log.e(
                "FCM",
                "Başlık : " + baslik + "İçerik : $icerik" + " Bildirim_turu: $bildirim_turu" + " Secilen sohbet odası:" + sohbet_odasi_id
            )
            var ref = FirebaseDatabase.getInstance().reference
                .child("sohbet_odasi")
                .orderByKey()
                .equalTo(sohbet_odasi_id).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError?) {

                    }

                    override fun onDataChange(dataSnapshot: DataSnapshot?) {
                        var tekSohbetOdasi = dataSnapshot?.children?.iterator()?.next()

                        var oAnkiSohbetOdasi = SohbetOdasi()
                        var nesneMap = (tekSohbetOdasi?.getValue() as HashMap<String, Object>)

                        oAnkiSohbetOdasi.sohbetOdasi_id = nesneMap.get("sohbetOdasi_id").toString()
                        oAnkiSohbetOdasi.sohbetOdasi_adi = nesneMap.get("sohbetOdasi_adi").toString()
                        oAnkiSohbetOdasi.seviye = nesneMap.get("seviye").toString()
                        oAnkiSohbetOdasi.olusturan_id = nesneMap.get("olusturan_id").toString()


                        var gorulenMesajSayisi = tekSohbetOdasi.child("odadaki_kullanicilar")
                            .child(FirebaseAuth.getInstance().currentUser?.uid)
                            .child("okunan_mesaj_sayisi")
                            .getValue().toString().toInt()


                        var toplamMesaj = tekSohbetOdasi.child("sohbet_odasi_mesajlari").childrenCount.toInt()

                        okunmayiBeklenenMesajSayisi = toplamMesaj - gorulenMesajSayisi

                        bildirimGonder(baslik, icerik, oAnkiSohbetOdasi)


                    }
                })
        }


    }

    private fun activityKontrolEt(): Boolean {
        return SohbetOdasiActivity.activityAcikMi
    }

    private fun bildirimGonder(baslik: String?, icerik: String?, oAnkiSohbetOdasi: SohbetOdasi) {

        var bildirimID = notificationIdOlustur(oAnkiSohbetOdasi.sohbetOdasi_id!!)

        Log.e("AAA", bildirimID.toString())

        var pendingIntent:Intent=Intent(this,MainActivity::class.java)
        pendingIntent.flags=Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        pendingIntent.putExtra("sohbet_odasi_id",oAnkiSohbetOdasi.sohbetOdasi_id!!)


        var bildirimPendingIntent=PendingIntent.getActivity(this,10,pendingIntent,PendingIntent.FLAG_UPDATE_CURRENT)



        var builder = NotificationCompat.Builder(this, oAnkiSohbetOdasi.sohbetOdasi_adi!!)
            .setSmallIcon(R.drawable.ic_action_user)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_action_user))
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setContentTitle(oAnkiSohbetOdasi.sohbetOdasi_adi + "odasından"+baslik)
            .setContentText("icerik")
            .setColor(resources.getColor(R.color.colorAccent))
            .setAutoCancel(true)
            .setSubText(""+okunmayiBeklenenMesajSayisi+"okunmayı bekleyen mesaj")
            .setStyle(NotificationCompat.BigTextStyle().bigText(icerik))
            .setNumber(okunmayiBeklenenMesajSayisi)
            .setOnlyAlertOnce(true)
            .setContentIntent(bildirimPendingIntent)

        var notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(bildirimID, builder.build())

    }

    private fun notificationIdOlustur(sohbetOdasiID: String): Int {
        var id = 0
        for (i in 4..8) {
            id = id + sohbetOdasiID[0].toInt()
        }
        return id
    }
}