package com.firebasekotlin

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.firebasekotlin.dialogs.SifremiUnuttumDialogFragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    lateinit var mAuthStateListener: FirebaseAuth.AuthStateListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        initMyAuthStateListener()

        tvKayıtOl.setOnClickListener {
            var intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        btnGirisYap.setOnClickListener {
            if (!etMail.text.isEmpty() && !etMail.text.isEmpty()) {
                progresBarGöster()
                FirebaseAuth.getInstance().signInWithEmailAndPassword(etMail.text.toString(), etSifre.text.toString())
                    .addOnCompleteListener(object : OnCompleteListener<AuthResult> {
                        override fun onComplete(p0: Task<AuthResult>) {
                            if (p0.isSuccessful) {
                                progresBarGizle()
                                Toast.makeText(
                                    this@LoginActivity,
                                    "Başarılı Giriş : " + FirebaseAuth.getInstance().currentUser?.email,
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                progresBarGizle()
                                Toast.makeText(
                                    this@LoginActivity,
                                    "Hatalı Giriş!!! : " + p0.exception?.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    })

            } else {
                Toast.makeText(this@LoginActivity, "Boş Alanlaru Doldurunuz!!!", Toast.LENGTH_SHORT).show()
            }
        }

        tvSifreTekrarYolla.setOnClickListener {
            var gosterDialogSifre= SifremiUnuttumDialogFragment()
            gosterDialogSifre.show(supportFragmentManager,"gosterDialogSifre")
        }
    }

    private fun progresBarGöster() {
        progressBarLogin.visibility = View.VISIBLE
    }

    private fun progresBarGizle() {
        progressBarLogin.visibility = View.INVISIBLE
    }

    private fun initMyAuthStateListener() {
        mAuthStateListener = object : FirebaseAuth.AuthStateListener {
            override fun onAuthStateChanged(p0: FirebaseAuth) {
                var kullanici = p0.currentUser
                if (kullanici != null) {
                    if (kullanici.isEmailVerified) {
                        var intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(
                            this@LoginActivity,
                            "Mail adresinizi onaylayın ve giriş yapın!!!",
                            Toast.LENGTH_SHORT
                        ).show()
                        FirebaseAuth.getInstance().signOut()
                    }
                } else {

                }
            }
        }

    }

    override fun onStart() {
        super.onStart()
        FirebaseAuth.getInstance().addAuthStateListener(mAuthStateListener)
    }

    override fun onStop() {
        super.onStop()
        FirebaseAuth.getInstance().removeAuthStateListener(mAuthStateListener)
    }


}
