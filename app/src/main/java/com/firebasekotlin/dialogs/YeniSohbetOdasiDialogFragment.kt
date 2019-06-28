package com.firebasekotlin.dialogs


import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.firebasekotlin.R
import com.firebasekotlin.SohbetActivity
import com.firebasekotlin.model.Kullanici
import com.firebasekotlin.model.SohbetMesaj
import com.firebasekotlin.model.SohbetOdasi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*


class YeniSohbetOdasiDialogFragment : DialogFragment() {

    lateinit var etSohbetOdasiAdi: EditText
    lateinit var btnSohbetOdasiOlustur: Button
    lateinit var seekBarSeviye: SeekBar
    lateinit var tvKullaniciSeviye: TextView

    var mSeekProgress = 0
    var kullaniciSeviye = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater.inflate(R.layout.fragment_yeni_sohbet_odasi_dialog, container, false)

        etSohbetOdasiAdi = view.findViewById(R.id.etYeniSohbetOdasiAdi)
        btnSohbetOdasiOlustur = view.findViewById(R.id.btnYeniSohbetOdasiOlustur)
        seekBarSeviye = view.findViewById(R.id.seekBarSeviye)
        tvKullaniciSeviye = view.findViewById(R.id.tvYeniSohbetSeviye)
        tvKullaniciSeviye.setText(mSeekProgress.toString())


        seekBarSeviye.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStopTrackingTouch(p0: SeekBar?) {
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onProgressChanged(p0: SeekBar?, progress: Int, p2: Boolean) {
                mSeekProgress = progress
                tvKullaniciSeviye.text = mSeekProgress.toString()
            }

        })

        kullaniciSeviyeBilgisiGetir()

        /**
         * sohbetOdasiOluşturma
         */
        btnSohbetOdasiOlustur.setOnClickListener {
            if (!etSohbetOdasiAdi.text.isNullOrEmpty()) {


                if (kullaniciSeviye >= seekBarSeviye.progress) {
                    /**
                     * FirebaseDatabase yeni bir alan ekleme
                     */
                    var ref = FirebaseDatabase.getInstance().reference
                    var sohbotOdasiID = ref.child("sohbet_odasi").push().key

                    var yeniSohbetOdasi = SohbetOdasi()
                    yeniSohbetOdasi.olusturan_id = FirebaseAuth.getInstance()?.currentUser?.uid
                    yeniSohbetOdasi.seviye = mSeekProgress.toString()
                    yeniSohbetOdasi.sohbetOdasi_adi = etSohbetOdasiAdi.text.toString()
                    yeniSohbetOdasi.sohbetOdasi_id = sohbotOdasiID

                    ref.child("sohbet_odasi").child(sohbotOdasiID!!).setValue(yeniSohbetOdasi)


                    /**
                     * FirebaseDatabase yeni bir alan ekleme
                     * Mesaj ekleme alani
                     */
                    var mesajID = ref.child("sohbet_odasi").push().key
                    var karsilamaMesaji = SohbetMesaj()
                    karsilamaMesaji.mesaj = "Sohbet odasina hoşgeldiniz."
                    karsilamaMesaji.timestamp = getMesajTarihi()
                    ref.child("sohbet_odasi")
                        .child(sohbotOdasiID)
                        .child("sohbet_odasi_mesajlari")
                        .child(mesajID!!)
                        .setValue(karsilamaMesaji)

                    Toast.makeText(activity, "Sohbet Odasi Oluşturuldu.", Toast.LENGTH_SHORT).show()
                    (activity as SohbetActivity).init()
                    dialog.dismiss()




                } else {
                    Toast.makeText(
                        activity,
                        "Seviyeniz : " + kullaniciSeviye + " ve bu seviyeden yukarı sohbet odası oluşturamazsınız!",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } else {
                Toast.makeText(activity, "Sohbet Odasi Adını Yazınız!", Toast.LENGTH_SHORT).show()
            }
        }


        return view
    }

    private fun kullaniciSeviyeBilgisiGetir() {
        var ref = FirebaseDatabase.getInstance().reference
        var sorgu = ref.child("kullanici").orderByKey().equalTo(FirebaseAuth.getInstance().currentUser?.uid)

        sorgu.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (tekKayit in dataSnapshot.children) {
                    //var k:Kullanici
                    //k=tekKayit.getValue(Kullanici::class.java)!!
                    kullaniciSeviye = tekKayit.getValue(Kullanici::class.java)?.seviye!!.toInt()

                }
            }
        })
    }

    private fun getMesajTarihi(): String {
        var sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale("tr"))
        return sdf.format(Date())
    }
}
