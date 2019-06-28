package com.firebasekotlin.dialogs


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.DialogFragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.firebasekotlin.R


class ProfilResmiKaydetFragment : DialogFragment() {

    lateinit var tvGaleridenSec: TextView
    lateinit var tvKameradanSec: TextView

    interface onProfilResimListener {
        fun getResimYolu(resimPath: Uri?)
        fun getResimBitmap(bitmap: Bitmap)
    }

    lateinit var myProfilResimListener: onProfilResimListener

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater.inflate(R.layout.fragment_profil_resmi_kaydet, container, false)


        tvGaleridenSec = view.findViewById(R.id.tvYeniGaleridenFoto)
        tvKameradanSec = view.findViewById(R.id.tvYeniKameradanFoto)

        tvGaleridenSec.setOnClickListener {

            var intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, 100)

        }

        tvKameradanSec.setOnClickListener {
            var intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, 200)

        }


        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //galeriden
        if (requestCode == 100 && resultCode == Activity.RESULT_OK && data != null) {
            var galeridenSecilenResimYolu = data.data
            myProfilResimListener.getResimYolu(galeridenSecilenResimYolu)
            Log.e("ERCIN", "" + galeridenSecilenResimYolu)
            dialog.dismiss()
        }
        //kameradan
        else if (requestCode == 200 && resultCode == Activity.RESULT_OK && data != null) {
            var kameradanCekilenResim: Bitmap
            kameradanCekilenResim = data.extras.get("data") as Bitmap
            myProfilResimListener.getResimBitmap(kameradanCekilenResim)
            dialog.dismiss()
        }
    }

    override fun onAttach(context: Context?) {
        myProfilResimListener = activity as onProfilResimListener
        super.onAttach(context)
    }


}
