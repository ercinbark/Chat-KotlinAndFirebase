package com.firebasekotlin.dialogs


import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.firebasekotlin.R
import com.google.firebase.auth.FirebaseAuth

class SifremiUnuttumDialogFragment : DialogFragment() {

    lateinit var emailEdittext:EditText

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        var view=inflater.inflate(R.layout.fragment_sifremi_unuttum_dialog, container, false)
        emailEdittext=view.findViewById(R.id.etSifreyiTekrarGonder)

        var btnIptal=view.findViewById<Button>(R.id.btnSifreyiUnuttumIptal)
        btnIptal.setOnClickListener {
            dialog.dismiss()
        }

        var btnGonder=view.findViewById<Button>(R.id.btnSifreyiUnuttumGonder)
        btnGonder.setOnClickListener {
            FirebaseAuth.getInstance().sendPasswordResetEmail(emailEdittext.text.toString())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful){
                        Toast.makeText(activity,"Sifre sıfırlama maili gönderildi.",Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }else{
                        Toast.makeText(activity,"Hata Oluştu : "+task.exception?.message,Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                }
        }

        return view
    }

}
