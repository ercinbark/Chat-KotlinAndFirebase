package com.firebasekotlin

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import com.firebasekotlin.adapter.SohbetOdasiRecyclerViewAdapter
import com.firebasekotlin.dialogs.YeniSohbetOdasiDialogFragment
import com.firebasekotlin.model.SohbetMesaj
import com.firebasekotlin.model.SohbetOdasi
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_sohbet.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class SohbetActivity : AppCompatActivity() {
    lateinit var tumSohbetOdalari: ArrayList<SohbetOdasi>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sohbet)

        init()

    }

    fun init() {
        tumSohbetOdalariniGetir()
        fabYeniSohbetOdasi.setOnClickListener {
            var dialog = YeniSohbetOdasiDialogFragment()
            dialog.show(supportFragmentManager, "gösterYeniSohbetOdasi")
        }

    }

    private fun tumSohbetOdalariniGetir() {
        tumSohbetOdalari = ArrayList<SohbetOdasi>()

        var ref = FirebaseDatabase.getInstance().reference
        var sorgu = ref.child("sohbet_odasi").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                /**
                 * sohbet odasi düğümünde bulunan sohbet odalarini getirir!!!
                 */

                for (tekSohbetOdasi in dataSnapshot.children) {
                    /**
                     * hashmap yapmamızın nedeni arrayList dönmesi
                     */
                    var oAnkiSohbetOdasi = SohbetOdasi()
                    var nesneMap = (tekSohbetOdasi.getValue() as HashMap<String, Object>)

                    oAnkiSohbetOdasi.sohbetOdasi_id = nesneMap.get("sohbetOdasi_id").toString()
                    oAnkiSohbetOdasi.sohbetOdasi_adi = nesneMap.get("sohbetOdasi_adi").toString()
                    oAnkiSohbetOdasi.seviye = nesneMap.get("seviye").toString()
                    oAnkiSohbetOdasi.olusturan_id = nesneMap.get("olusturan_id").toString()


                    /**
                     * sohbet odasi düğümünde bulunan sohbet_odasi_mesajlarini getirir!!!
                     * hashmap yok (list dönmediği için)
                     */
                    var tumMesajlar = ArrayList<SohbetMesaj>()
                    for (mesajlar in tekSohbetOdasi.child("sohbet_odasi_mesajlari").children) {

                        var okunanMesaj = SohbetMesaj()
                        okunanMesaj.timestamp = mesajlar.getValue(SohbetMesaj::class.java)?.timestamp
                        okunanMesaj.adi = mesajlar.getValue(SohbetMesaj::class.java)?.adi
                        okunanMesaj.kullanici_id = mesajlar.getValue(SohbetMesaj::class.java)?.kullanici_id
                        okunanMesaj.mesaj = mesajlar.getValue(SohbetMesaj::class.java)?.mesaj
                        okunanMesaj.profil_resmi = mesajlar.getValue(SohbetMesaj::class.java)?.profil_resmi

                        tumMesajlar.add(okunanMesaj)
                    }

                    oAnkiSohbetOdasi.sohbet_odasi_mesajlari = tumMesajlar
                    tumSohbetOdalari.add(oAnkiSohbetOdasi)
                }

                Toast.makeText(
                    this@SohbetActivity,
                    "Tüm sohbet odasi sayısı : " + tumSohbetOdalari.size,
                    Toast.LENGTH_SHORT
                ).show()

                rcSohbetOdalari.layoutManager= LinearLayoutManager(this@SohbetActivity) as RecyclerView.LayoutManager?
                rcSohbetOdalari.adapter=SohbetOdasiRecyclerViewAdapter(tumSohbetOdalari)
            }

        })

    }
}
