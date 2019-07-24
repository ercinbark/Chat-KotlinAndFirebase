package com.firebasekotlin

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import com.firebasekotlin.adapter.SohbetMesajRecyclerViewAdapter
import com.firebasekotlin.interfaces.FCMInterface
import com.firebasekotlin.model.FCMModel
import com.firebasekotlin.model.Kullanici
import com.firebasekotlin.model.SohbetMesaj
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_sohbet_odasi.*
import kotlinx.android.synthetic.main.tek_satir_sohbet_odasi.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet

class SohbetOdasiActivity : AppCompatActivity() {

    companion object {
        var activityAcikMi: Boolean = false
    }

    //Firebase
    var mAuthListener: FirebaseAuth.AuthStateListener? = null
    var myMesajReferans: DatabaseReference? = null
    var SERVER_KEY: String? = null

    var secilenSohbetOdasiID: String? = null
    var tumMesajlar: ArrayList<SohbetMesaj>? = null
    var mesajIDSet: HashSet<String>? = null
    var myAdapter: SohbetMesajRecyclerViewAdapter? = null

    var BASE_URL = "https://fcm.googleapis.com/fcm/"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sohbet_odasi)

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


                /**
                 * mesaj atılan sohbet odasında,kullanıcılara bildirim gönderme işlemi
                 */
                var retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(
                        GsonConverterFactory.create()
                    ).build()

                var myInterface = retrofit.create(FCMInterface::class.java)

                var headers = HashMap<String, String>()
                headers.put("Content-Type", "application/json")
                headers.put("Authorization", "key=" + SERVER_KEY)


                var ref = FirebaseDatabase.getInstance().reference
                    .child("sohbet_odasi")
                    .child(secilenSohbetOdasiID)
                    .child("odadaki_kullanicilar")
                    .orderByKey().addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError?) {

                        }

                        override fun onDataChange(dataSnapshot: DataSnapshot?) {
                            for (kullaniciID in dataSnapshot!!.children) {
                                var id = kullaniciID.key

                                if (!id.equals(FirebaseAuth.getInstance().currentUser?.uid)) {

                                    var ref = FirebaseDatabase.getInstance().reference
                                        .child("kullanici")
                                        .orderByKey()
                                        .equalTo(id).addListenerForSingleValueEvent(object : ValueEventListener {
                                            override fun onCancelled(p0: DatabaseError?) {

                                            }

                                            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                                                var tekKullanici = dataSnapshot?.children?.iterator()?.next()

                                                var kullaniciMesajToken = tekKullanici?.getValue(Kullanici::class.java)?.mesaj_token


                                                var data = FCMModel.Data(
                                                    "Yeni Mesaj",
                                                    etYeniMesaj.text.toString(),
                                                    "Sohbet",
                                                    secilenSohbetOdasiID
                                                )

                                                var bildirim = FCMModel(kullaniciMesajToken!!, data)

                                                var istek = myInterface.bildirimleriGonder(headers, bildirim)
                                                istek.enqueue(object : Callback<Response<FCMModel>> {
                                                    override fun onFailure(
                                                        call: Call<Response<FCMModel>>,
                                                        t: Throwable
                                                    ) {
                                                        Log.e("RETROFIT", "HATA : " + t.message)
                                                    }

                                                    override fun onResponse(
                                                        call: Call<Response<FCMModel>>,
                                                        response: Response<Response<FCMModel>>
                                                    ) {
                                                        Log.e("RETROFIT", "BASARILI : " + response.toString())
                                                    }
                                                })

                                                etYeniMesaj.setText("")

                                            }
                                        })

                                }
                            }
                        }

                    })

            }
        }
    }

    private fun getMesajTarihi(): String {
        var sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale("tr"))
        return sdf.format(Date())
    }

    private fun bildirimeGoreListele() {
        var gelenIntent = intent
        Log.e("sID", "SohbetOdasiActivity : " + gelenIntent.getStringExtra("sohbet_odasi_id"))
        if (intent.hasExtra("sohbet_odasi_id")) {
            secilenSohbetOdasiID = intent.getStringExtra("sohbet_odasi_id")
            baslatMesajListener()

        }


    }

    private fun sohbetOdasiniOgren() {
        //secilenSohbetOdasiID = intent.getStringExtra("sohbetOdasiId")
        //baslatMesajListener()

        /**
         * Notification ile açıldığında tetiklenir
         */
        if (intent.getStringExtra("sohbet_odasi_id") != null) {
            secilenSohbetOdasiID = intent.getStringExtra("sohbet_odasi_id")
            Log.e("sID", "Notificaiton : " + intent.getStringExtra("sohbet_odasi_id"))
        } else {
            /**
             * SohbetOdasi listesinden seçim yapıldığında tetiklenir
             */
            secilenSohbetOdasiID = intent.getStringExtra("sohbetOdasiId")
            Log.e("sID", "OnClickRecyclerView : " + intent.getStringExtra("sohbetOdasiId"))
            var secilenSohbetOdasiAdi: String? = null
            var ref = FirebaseDatabase.getInstance().reference
                .child("sohbet_odasi")
                .child(secilenSohbetOdasiID)
                .child("sohbetodasi_adi")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError?) {

                    }

                    override fun onDataChange(p0: DataSnapshot?) {
                        //var singleSnapShot = p0?.children?.iterator()?.next()
                        secilenSohbetOdasiAdi = p0?.value.toString()
                        //tvSohbetOdasiAdi.text=secilenSohbetOdasiAdi
                    }

                })
        }

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
            if (activityAcikMi)
                gorunenMesajSayisiniGuncelle(p0?.childrenCount.toInt())
        }
    }

    private fun gorunenMesajSayisiniGuncelle(toplamMesaj: Int) {
        var ref = FirebaseDatabase.getInstance().reference
            .child("sohbet_odasi")
            .child(secilenSohbetOdasiID)
            .child("odadaki_kullanicilar")
            .child(FirebaseAuth.getInstance().currentUser?.uid)
            .child("okunan_mesaj_sayisi")
            .setValue(toplamMesaj)
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
        activityAcikMi = true
        FirebaseAuth.getInstance().addAuthStateListener(mAuthListener!!)
    }

    override fun onStop() {
        super.onStop()
        activityAcikMi = false
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
}
