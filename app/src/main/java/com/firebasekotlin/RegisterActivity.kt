package com.firebasekotlin

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.firebasekotlin.model.Kullanici
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)


        btnKayıtOl.setOnClickListener {
            if (!etMail.text.toString().isEmpty() && !etSifre.text.toString().isEmpty() && !etSifreTekrar.text.toString().isEmpty()) {
                if (etSifre.text.toString().equals(etSifreTekrar.text.toString())) {

                    yeniUyeKayit(etMail.text.toString(), etSifre.text.toString())

                } else {
                    Toast.makeText(this, "Şifreler Aynı Değil!!!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Boş Alanları Doldurunuz!!!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun yeniUyeKayit(mail: String, sifre: String) {
        progresBarGöster()
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(mail, sifre)
            .addOnCompleteListener(object : OnCompleteListener<AuthResult> {
                override fun onComplete(p0: Task<AuthResult>) {
                    if (p0.isSuccessful) {
                        progresBarGizle()
                        onayMailiGönder()
                        /**
                         * kullanıcıyı veritabanına kaydetme
                         */

                        val veritabaninaEklenecekKullanici = Kullanici()
                        veritabaninaEklenecekKullanici.isim = etMail.text.toString().substring(0, etMail.text.toString().indexOf("@"))
                        veritabaninaEklenecekKullanici.kullanici_id = FirebaseAuth.getInstance().currentUser?.uid
                        veritabaninaEklenecekKullanici.profil_resmi = ""
                        veritabaninaEklenecekKullanici.telefon = "123"
                        veritabaninaEklenecekKullanici.seviye = "1"

                        FirebaseDatabase.getInstance().reference
                            .child("kullanici")
                            .child(FirebaseAuth.getInstance().currentUser?.uid.toString())
                            .setValue(veritabaninaEklenecekKullanici).addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(
                                        this@RegisterActivity,
                                        "Üye Kaydedildi." + FirebaseAuth.getInstance().currentUser?.uid,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    FirebaseAuth.getInstance().signOut()
                                    loginSayfasınaYonlendir()
                                }
                            }
                    } else {
                        progresBarGizle()
                        Toast.makeText(
                            this@RegisterActivity,
                            "Üye Kaydedilirken hata oluştu!!!" + p0.exception?.message,
                            Toast.LENGTH_SHORT
                        )
                            .show()

                    }

                }
            })

    }

    private fun onayMailiGönder() {
        var kullanici = FirebaseAuth.getInstance().currentUser
        kullanici?.sendEmailVerification()?.addOnCompleteListener(object : OnCompleteListener<Void> {
            override fun onComplete(p0: Task<Void>) {
                if (p0.isSuccessful) {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Mail kutunuzu kontrol edin,mailinizi onaylayın",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Mail gönderirken hata oluştu." + p0?.exception?.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        })
    }

    private fun progresBarGöster() {
        progressBar.visibility = View.VISIBLE
    }

    private fun progresBarGizle() {
        progressBar.visibility = View.INVISIBLE
    }

    private fun loginSayfasınaYonlendir() {
        var intent = Intent(this@RegisterActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}
