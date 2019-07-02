package com.firebasekotlin

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import com.bumptech.glide.Glide.init
import com.firebasekotlin.adapter.SohbetMesajRecyclerViewAdapter
import com.firebasekotlin.model.Kullanici
import com.firebasekotlin.model.SohbetMesaj
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_sohbet_odasi.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

class SohbetOdasiActivity : AppCompatActivity() {

    //Firebase
    var mAuthListener: FirebaseAuth.AuthStateListener? = null
    var myMesajReferans: DatabaseReference? = null
    var SERVER_KEY: String? = null

    var secilenSohbetOdasiID: String? = null
    var tumMesajlar: ArrayList<SohbetMesaj>? = null
    var mesajIDSet: HashSet<String>? = null
    var myAdapter: SohbetMesajRecyclerViewAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sohbet_odasi)
        //sohbetOdasiMesajlariGetir()

        //kullanıcın giriş-çıkıs işlemlerini dinler
        baslatFirebaseAuthListener()

        //sohbet activityden gelen seçilen sohbet odasinin id bilgisini alır ve valueEventListener kaydı yapar
        sohbetOdasiniOgren()

        //serverKey okuma()
        serverKeyOku()

        //mesajGönder
        initMesajGönder()

    }

    private fun serverKeyOku() {
        var ref = FirebaseDatabase.getInstance().reference
            .child("server")
            .orderByValue()
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError?) {

            }

            override fun onDataChange(p0: DataSnapshot?) {
                var singleSnapShot = p0?.children?.iterator()?.next()
                SERVER_KEY = singleSnapShot!!.getValue().toString()
                Log.e("SERVER_KEY", SERVER_KEY)
            }
        })
    }

    private fun initMesajGönder() {
        btnMesajGonder.setOnClickListener {
            if (!etYeniMesaj.text.toString().equals("")) {

                var yazılanMesaj = etYeniMesaj.text.toString()

                var kaydedilecekMesaj = SohbetMesaj()
                kaydedilecekMesaj.mesaj = yazılanMesaj
                kaydedilecekMesaj.kullanici_id = FirebaseAuth.getInstance().currentUser?.uid
                kaydedilecekMesaj.timestamp = getMesajTarihi()


                var referans = FirebaseDatabase.getInstance().reference
                    .child("sohbet_odasi")
                    .child(secilenSohbetOdasiID!!)
                    .child("sohbet_odasi_mesajlari")


                var yeniMesaj = referans.push().key
                referans.child(yeniMesaj!!).setValue(kaydedilecekMesaj)

                etYeniMesaj.setText("")

            }


        }


        etYeniMesaj.setOnClickListener {
            rcMesajlar.smoothScrollToPosition(myAdapter!!.itemCount - 1)
        }
    }

    private fun getMesajTarihi(): String {
        var sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale("tr"))
        return sdf.format(Date())
    }

    private fun sohbetOdasiniOgren() {
        secilenSohbetOdasiID = intent.getStringExtra("sohbetOdasiId")
        baslatMesajListener()


    }

    private fun baslatMesajListener() {
        myMesajReferans = FirebaseDatabase.getInstance().getReference().child("sohbet_odasi")
            .child(secilenSohbetOdasiID!!)
            .child("sohbet_odasi_mesajlari")


        myMesajReferans?.addValueEventListener(myValueEventListener)
    }

    var myValueEventListener: ValueEventListener = object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {

        }

        override fun onDataChange(p0: DataSnapshot) {
            sohbetOdasindakiMesajlariGetir()
        }
    }

    private fun sohbetOdasindakiMesajlariGetir() {

        if (tumMesajlar == null) {
            tumMesajlar = ArrayList<SohbetMesaj>()
            mesajIDSet = HashSet<String>()

        }

        myMesajReferans = FirebaseDatabase.getInstance().reference
        var sorgu = myMesajReferans
            ?.child("sohbet_odasi")
            ?.child(secilenSohbetOdasiID!!)
            ?.child("sohbet_odasi_mesajlari")!!.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (tekMesaj in dataSnapshot.children) {
                    var geciciMesajlar = SohbetMesaj()
                    var kullaniciID = tekMesaj.getValue(SohbetMesaj::class.java)?.kullanici_id


                    if (!mesajIDSet!!.contains(tekMesaj.key)) {

                        mesajIDSet!!.add(tekMesaj.key.toString())

                        if (kullaniciID != null) {
                            geciciMesajlar.mesaj = tekMesaj.getValue(SohbetMesaj::class.java)?.mesaj
                            geciciMesajlar.kullanici_id = tekMesaj.getValue(SohbetMesaj::class.java)?.kullanici_id
                            geciciMesajlar.timestamp = tekMesaj.getValue(SohbetMesaj::class.java)?.timestamp


                            var kullaniciDetaylari = myMesajReferans
                                ?.child("kullanici")
                                ?.orderByKey()
                                ?.equalTo(kullaniciID)
                            kullaniciDetaylari?.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onCancelled(p0: DatabaseError) {

                                }

                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    var buluunanKullanici = dataSnapshot.children.iterator().next()
                                    geciciMesajlar.profil_resmi =
                                        buluunanKullanici.getValue(Kullanici::class.java)?.profil_resmi
                                    geciciMesajlar.adi = buluunanKullanici.getValue(Kullanici::class.java)?.isim

                                    myAdapter?.notifyDataSetChanged()
                                }
                            })

                            tumMesajlar?.add(geciciMesajlar)
                            myAdapter?.notifyDataSetChanged()
                            rcMesajlar.scrollToPosition(myAdapter!!.itemCount - 1)

                        } else {
                            geciciMesajlar.mesaj = tekMesaj.getValue(SohbetMesaj::class.java)?.mesaj
                            geciciMesajlar.timestamp = tekMesaj.getValue(SohbetMesaj::class.java)?.timestamp
                            geciciMesajlar.adi = ""
                            geciciMesajlar.profil_resmi = ""
                            tumMesajlar?.add(geciciMesajlar)
                            myAdapter?.notifyDataSetChanged()
                        }


                    }


                }
            }
        })

        if (myAdapter == null) {
            initMesajlarListesi()
        }


    }

    private fun initMesajlarListesi() {
        myAdapter = SohbetMesajRecyclerViewAdapter(this, tumMesajlar!!)
        rcMesajlar.adapter = myAdapter
        rcMesajlar.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rcMesajlar.scrollToPosition(myAdapter?.itemCount!! - 1)
    }


    private fun baslatFirebaseAuthListener() {
        mAuthListener = object : FirebaseAuth.AuthStateListener {
            override fun onAuthStateChanged(p0: FirebaseAuth) {
                var kullanici = p0.currentUser

                if (kullanici == null) {
                    var intent = Intent(this@SohbetOdasiActivity, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        FirebaseAuth.getInstance().addAuthStateListener(mAuthListener!!)
    }

    override fun onStop() {
        super.onStop()
        if (mAuthListener != null) {
            FirebaseAuth.getInstance().removeAuthStateListener(mAuthListener!!)
        }
    }

    override fun onResume() {
        super.onResume()
        kullaniciyiKontrolEt()
    }

    private fun kullaniciyiKontrolEt() {
        var kullanici = FirebaseAuth.getInstance().currentUser
        if (kullanici == null) {
            var intent = Intent(this@SohbetOdasiActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    /*private fun sohbetOdasiMesajlariGetir() {
        var secilenSohbetOdasiID = intent.getStringExtra("sohbetOdasiId")
        tumMesajlar = ArrayList<SohbetMesaj>()

        var ref = FirebaseDatabase.getInstance().reference

        var sorgu = ref.child("sohbet_odasi")
            .child(secilenSohbetOdasiID)
            .child("sohbet_odasi_mesajlari")

        sorgu.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (mesaj in dataSnapshot.children) {
                    var eklenecekMesaj = SohbetMesaj()
                    var kullaniciID = mesaj.getValue(SohbetMesaj::class.java)?.kullanici_id

                    if (kullaniciID != null) {
                        eklenecekMesaj.kullanici_id = kullaniciID
                        eklenecekMesaj.mesaj = mesaj.getValue(SohbetMesaj::class.java)?.mesaj
                        eklenecekMesaj.timestamp = mesaj.getValue(SohbetMesaj::class.java)?.timestamp

                        tumMesajlar.add(eklenecekMesaj)
                    } else {
                        eklenecekMesaj.mesaj = mesaj.getValue(SohbetMesaj::class.java)?.mesaj
                        eklenecekMesaj.timestamp = mesaj.getValue(SohbetMesaj::class.java)?.timestamp

                        tumMesajlar.add(eklenecekMesaj)
                    }

                }
            }
        })

    }*/
}
