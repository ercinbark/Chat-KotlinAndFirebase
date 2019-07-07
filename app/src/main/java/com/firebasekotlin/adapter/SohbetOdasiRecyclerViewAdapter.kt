package com.firebasekotlin.adapter

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import com.bumptech.glide.Glide
import com.firebasekotlin.R
import com.firebasekotlin.SohbetActivity
import com.firebasekotlin.SohbetOdasiActivity
import com.firebasekotlin.model.Kullanici
import com.firebasekotlin.model.SohbetOdasi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.tek_satir_sohbet_odasi.view.*

class SohbetOdasiRecyclerViewAdapter(mActivity: AppCompatActivity, tumSohbetOdalari: ArrayList<SohbetOdasi>) :
    RecyclerView.Adapter<SohbetOdasiRecyclerViewAdapter.SohbetOdasiHolder>() {

    var sohbetOdalari = tumSohbetOdalari
    var myActivity = mActivity

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): SohbetOdasiHolder {
        var inflater = LayoutInflater.from(parent?.context)
        var tekSatirSohbetOdalari = inflater.inflate(R.layout.tek_satir_sohbet_odasi, parent, false)

        return SohbetOdasiHolder(tekSatirSohbetOdalari)
    }


    override fun getItemCount(): Int {
        return sohbetOdalari.size
    }

    override fun onBindViewHolder(holder: SohbetOdasiHolder, position: Int) {
        var oAnOlusturulanSohbetOdasi = sohbetOdalari.get(position)
        holder.setData(oAnOlusturulanSohbetOdasi, position)
    }

    inner class SohbetOdasiHolder(itemview: View?) : RecyclerView.ViewHolder(itemview!!) {
        var tekSatirSohbetOdasiLayout = itemview as LinearLayout

        var sohbetOdasiOlusturan = tekSatirSohbetOdasiLayout.tvOlusturanAdi
        var sohbetOdasiResim = tekSatirSohbetOdasiLayout.imgProfilResmiSohbetOdasi
        var sohbetOdasiSil = tekSatirSohbetOdasiLayout.imgSohbetOdasiSil
        var sohbetOdasiMesajSayısi = tekSatirSohbetOdasiLayout.tvMesajSayisi
        var sohbetOdasiAdi = tekSatirSohbetOdasiLayout.tvSohbetOdasiAdi


        fun setData(oAnOlusturulanSohbetOdasi: SohbetOdasi, position: Int) {
            sohbetOdasiAdi.text = oAnOlusturulanSohbetOdasi.sohbetOdasi_adi
            sohbetOdasiMesajSayısi.text = (oAnOlusturulanSohbetOdasi.sohbet_odasi_mesajlari)?.size.toString()

            var ref = FirebaseDatabase.getInstance().reference
            var sorgu = ref.child("kullanici")
                .orderByKey()
                .equalTo(oAnOlusturulanSohbetOdasi.olusturan_id)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {

                    }

                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (kullanici in dataSnapshot.children) {
                            var profilResmi = kullanici.getValue(Kullanici::class.java)?.profil_resmi.toString()
                            Glide.with(itemView.context).load(profilResmi).into(sohbetOdasiResim)
                            sohbetOdasiOlusturan.text = kullanici.getValue(Kullanici::class.java)?.isim.toString()
                        }
                    }

                })


            sohbetOdasiSil.setOnClickListener {
                if (oAnOlusturulanSohbetOdasi.olusturan_id.equals(FirebaseAuth.getInstance().currentUser?.uid)) {

                    var dialog = AlertDialog.Builder(itemView.context)
                    dialog.setTitle("Sohbet Odası Sil")
                    dialog.setMessage("Emin misiniz?")
                    dialog.setCancelable(false)
                    dialog.setPositiveButton("Evet", object : DialogInterface.OnClickListener {
                        override fun onClick(p0: DialogInterface?, p1: Int) {
                            (myActivity as SohbetActivity).sohbetOdasiSil(oAnOlusturulanSohbetOdasi.sohbetOdasi_id.toString())
                        }
                    })
                    dialog.setNegativeButton("Hayır", object : DialogInterface.OnClickListener {
                        override fun onClick(p0: DialogInterface?, p1: Int) {

                        }
                    })
                    dialog.show()

                } else {
                    Toast.makeText(
                        itemView.context,
                        "Bu sohbet odasını sen olusturmadın,silemezsin",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            tekSatirSohbetOdasiLayout.setOnClickListener {

                /**
                 * sohbet odasina giren kullaniclarin tokenlarini kaydet
                 * bildirim için
                 * kullaniciyiSohbetOdasinaKaydet()
                 */
                kullaniciyiSohbetOdasinaKaydet(oAnOlusturulanSohbetOdasi)

                var intent = Intent(myActivity, SohbetOdasiActivity::class.java)
                intent.putExtra("sohbetOdasiId",oAnOlusturulanSohbetOdasi.sohbetOdasi_id)
                myActivity.startActivity(intent)


            }
        }

        private fun kullaniciyiSohbetOdasinaKaydet(oAnOlusturulanSohbetOdasi: SohbetOdasi) {

            var ref=FirebaseDatabase.getInstance().reference
                .child("sohbet_odasi")
                .child(oAnOlusturulanSohbetOdasi.sohbetOdasi_id)
                .child("odadaki_kullanicilar")
                .child(FirebaseAuth.getInstance().currentUser?.uid)
                .child("okunan_mesaj_sayisi")
                .setValue((oAnOlusturulanSohbetOdasi.sohbet_odasi_mesajlari)?.size.toString())

        }

    }
}