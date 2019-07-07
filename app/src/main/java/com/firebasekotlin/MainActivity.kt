package com.firebasekotlin

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    lateinit var myAuthStateListener: FirebaseAuth.AuthStateListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initAuthStateListener()
        initFCM()
        getPendingIntent()


    }

    private fun getPendingIntent() {
        var gelenIntent=intent

        if (gelenIntent.hasExtra("sohbet_odasi_id")){
            var intent=Intent(this,SohbetOdasiActivity::class.java)
            intent.putExtra("sohbet_odasi_id",gelenIntent.getStringExtra("sohbet_odasi_id"))
            startActivity(intent)
            Log.e("sID","MainActivity : "+gelenIntent.getStringExtra("sohbet_odasi_id"))
        }
    }

    private fun initFCM() {
        var token=FirebaseInstanceId.getInstance().token
        tokenVeriTabanianaKaydet(token)
    }

    private fun tokenVeriTabanianaKaydet(refreshedToken: String?) {
        var ref= FirebaseDatabase.getInstance().reference
            .child("kullanici")
            .child(FirebaseAuth.getInstance().currentUser?.uid)
            .child("mesaj_token")
            .setValue(refreshedToken)
    }

    private fun setKullaniciBilgileri() {
        var kullanici=FirebaseAuth.getInstance().currentUser
        if (kullanici!=null){
            tvKullaniciAdi.text=if (kullanici.displayName.isNullOrEmpty()) "Tanımlanmadı" else kullanici.displayName
            tvKullaniciEMail.text=kullanici.email
            tvKullaniciUId.text=kullanici.uid
        }
    }
    private fun initAuthStateListener() {
        myAuthStateListener = object : FirebaseAuth.AuthStateListener {
            override fun onAuthStateChanged(p0: FirebaseAuth) {
                var kullanici = p0.currentUser
                if (kullanici != null) {

                } else {
                    var intent = Intent(this@MainActivity, LoginActivity::class.java)
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                    finish()
                }
            }

        }
    }



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.anamenu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menuCıkısYap -> {
                cikisYap()
                return true
            }
            R.id.menuHesapAyarlari->{
                var intent=Intent(this@MainActivity,KullaniciAyarlarActivity::class.java)
                startActivity(intent)
                return true
            }

            R.id.menuSohbet->{
                var intent=Intent(this@MainActivity,SohbetActivity::class.java)
                startActivity(intent)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun cikisYap() {
        FirebaseAuth.getInstance().signOut()

    }



    private fun kullaniciyiKontrolEt() {
        var kullanici = FirebaseAuth.getInstance().currentUser
        if (kullanici == null) {
            var intent = Intent(this@MainActivity, LoginActivity::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        kullaniciyiKontrolEt()
        setKullaniciBilgileri()
    }

    override fun onStart() {
        super.onStart()
        FirebaseAuth.getInstance().addAuthStateListener(myAuthStateListener)
    }

    override fun onStop() {
        super.onStop()
        FirebaseAuth.getInstance().removeAuthStateListener(myAuthStateListener)
    }
}
