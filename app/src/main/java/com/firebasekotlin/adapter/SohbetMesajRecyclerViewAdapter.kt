package com.firebasekotlin.adapter

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.firebasekotlin.R
import com.firebasekotlin.model.SohbetMesaj
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.tek_satir_mesaj_layout.view.*

class SohbetMesajRecyclerViewAdapter(context: Context, tumMesajlar: ArrayList<SohbetMesaj>) :
    RecyclerView.Adapter<SohbetMesajRecyclerViewAdapter.SohbetMesajViewHolder>() {

    var myContext = context
    var myTumMesajlar = tumMesajlar

    override fun onCreateViewHolder(p0: ViewGroup, viewType: Int): SohbetMesajViewHolder {
        var inflater = LayoutInflater.from(myContext)

        var view: View? = null

        if (viewType == 2) {
            view = inflater.inflate(R.layout.tek_satir_mesaj_layout, p0, false)
        } else {
            view = inflater.inflate(R.layout.tek_satir_mesaj_layout2, p0, false)
        }

        return SohbetMesajViewHolder(view)
    }

    override fun getItemCount(): Int {
        return myTumMesajlar.size
    }

    override fun getItemViewType(position: Int): Int {
        if (myTumMesajlar.get(position).kullanici_id.equals(FirebaseAuth.getInstance().currentUser?.uid)) {
            return 1
        } else {
            return 2
        }
    }

    override fun onBindViewHolder(holder: SohbetMesajViewHolder, position: Int) {

        var oAnkiMesaj = myTumMesajlar.get(position)
        holder.setData(oAnkiMesaj, position)
    }

    inner class SohbetMesajViewHolder(itemview: View) : RecyclerView.ViewHolder(itemview) {

        var tumLayout = itemview as ConstraintLayout

        var profilResim = tumLayout.imgMesajProfilResim
        var mesaj = tumLayout.tvMesaj
        var isim = tumLayout.tvMesajUserAd
        var tarih = tumLayout.tvMesajTarih


        fun setData(oAnkiMesaj: SohbetMesaj, position: Int) {
            mesaj.text = oAnkiMesaj.mesaj
            isim.text = oAnkiMesaj.adi
            tarih.text = oAnkiMesaj.timestamp

            if (!oAnkiMesaj.profil_resmi.isNullOrEmpty()) {
                Glide.with(myContext).load(oAnkiMesaj.profil_resmi).into(profilResim)
            }

        }


    }

}