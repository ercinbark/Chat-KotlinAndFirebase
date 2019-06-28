package com.firebasekotlin

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.firebasekotlin.dialogs.ProfilResmiKaydetFragment
import com.firebasekotlin.model.Kullanici
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_kullanici_ayarlar.*
import java.io.ByteArrayOutputStream

class KullaniciAyarlarActivity : AppCompatActivity(), ProfilResmiKaydetFragment.onProfilResimListener {
    var izinlerVerildi = false
    var galeridenGelenUrl: Uri? = null
    var kameranGelenBitmap: Bitmap? = null

    val MEGABYTE = 1000000.toDouble()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kullanici_ayarlar)

        var kullanici = FirebaseAuth.getInstance().currentUser!!

        kullaniciBilgileriniOku()

        btnSifreGonder.setOnClickListener {
            FirebaseAuth.getInstance().sendPasswordResetEmail(FirebaseAuth.getInstance().currentUser?.email.toString())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            this@KullaniciAyarlarActivity,
                            "Sifre sıfırlama maili gönderildi.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@KullaniciAyarlarActivity,
                            "Hata Oluştu : " + task.exception?.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }

        btnDegisiklileriKaydet.setOnClickListener {
            kullanici = FirebaseAuth.getInstance().currentUser!!
            if (etKullaniciAdi.text.toString().isNotEmpty()) {

                var bilgileriGuncelle = UserProfileChangeRequest.Builder()
                    .setDisplayName(etKullaniciAdi.text.toString())
                    .build()
                kullanici.updateProfile(bilgileriGuncelle)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            FirebaseDatabase.getInstance().reference
                                .child("kullanici")
                                .child(FirebaseAuth.getInstance().currentUser?.uid.toString())
                                .child("isim")
                                .setValue(etKullaniciAdi.text.toString())
                        }
                    }
            } else {
                Toast.makeText(this@KullaniciAyarlarActivity, "Kullanıcı adını doldurunuz!!!", Toast.LENGTH_SHORT)
                    .show()
            }

            if (etKullaniciTelefon.text.isNotEmpty()) run {
                FirebaseDatabase.getInstance().reference
                    .child("kullanici")
                    .child(FirebaseAuth.getInstance().currentUser?.uid.toString())
                    .child("telefon")
                    .setValue(etKullaniciTelefon.text.toString())
            }


            /**
             * fotograf kaydetme işlemi
             */

            if (galeridenGelenUrl != null) {
                fotografCompressed(galeridenGelenUrl!!)
            } else if (kameranGelenBitmap != null) {
                fotografCompressed(kameranGelenBitmap!!)
            }
        }

        btnSifreVeyaMailGuncelle.setOnClickListener {
            if (etDetaySifre.text.toString().isNotEmpty()) {

                var credential =
                    EmailAuthProvider.getCredential(kullanici.email.toString(), etDetaySifre.text.toString())
                kullanici.reauthenticate(credential).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        guncelleLayout.visibility = View.VISIBLE

                        btnMailiGuncelle.setOnClickListener {
                            mailAdresiniGüncelle()
                        }

                        btnSifreyiGuncelle.setOnClickListener {
                            sifreBilgisiniGuncelle()
                        }

                    } else {
                        Toast.makeText(
                            this@KullaniciAyarlarActivity,
                            "Şuanki şifrenizi yanlış girdiniz!!!",
                            Toast.LENGTH_SHORT
                        ).show()
                        guncelleLayout.visibility = View.INVISIBLE
                    }
                }


            } else {
                Toast.makeText(
                    this@KullaniciAyarlarActivity,
                    "Güncellemeler için geçerli sifrenizi yazmalısınız!!!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        /**
         * profilResmi
         */
        ivProfilResmi.setOnClickListener {

            if (izinlerVerildi) {
                var dialog = ProfilResmiKaydetFragment()
                dialog.show(supportFragmentManager, "fotoSec")
            } else {
                izinleriIste()
            }


        }
    }

    private fun kullaniciBilgileriniOku() {
        var veritabaniReferansı = FirebaseDatabase.getInstance().reference
        var kullanici = FirebaseAuth.getInstance().currentUser
        etMailAdresi.setText(kullanici?.email)

        /**
         * query 1
         * kullanici referansı altında olan keyleri sırala ve currentUser(o an giriş yapmış olan) id ye eşit olanı kullaniciyi bul getir
         */
        var sorgu = veritabaniReferansı.child("kullanici")
            .orderByKey()
            .equalTo(kullanici?.uid)

        sorgu.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(datasnapShot: DataSnapshot) {
                /**
                 * datasnapShot ile alınan veriler Kullanici modeline atama
                 */
                for (singleSnapshot in datasnapShot.children) {
                    var okunanKullanici = singleSnapshot.getValue(Kullanici::class.java)
                    Log.e("FIREBASE", "Adı : " + okunanKullanici?.isim)
                    Log.e("FIREBASE", "Telefon : " + okunanKullanici?.telefon)
                    Log.e("FIREBASE", "UserID : " + okunanKullanici?.kullanici_id)
                    Log.e("FIREBASE", "Seviye : " + okunanKullanici?.seviye)

                    etKullaniciAdi.setText(okunanKullanici?.isim)
                    etKullaniciTelefon.setText(okunanKullanici?.telefon)
                    Glide.with(this@KullaniciAyarlarActivity).load(okunanKullanici?.profil_resmi).into(ivProfilResmi)

                }
            }
        })

        /**
         * query 2
         */
        var sorgu2 = veritabaniReferansı.child("kullanici")
            .orderByChild("kullanici_id")
            .equalTo(kullanici?.uid)

        sorgu2.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(datasnapShot: DataSnapshot) {
                for (singleSnapshot in datasnapShot.children) {
                    var okunanKullanici = singleSnapshot.getValue(Kullanici::class.java)
                    Log.e("FIREBASE2", "Adı : " + okunanKullanici?.isim)
                    Log.e("FIREBASE2", "Telefon : " + okunanKullanici?.telefon)
                    Log.e("FIREBASE2", "UserID : " + okunanKullanici?.kullanici_id)
                    Log.e("FIREBASE2", "Seviye : " + okunanKullanici?.seviye)

                }
            }
        })

    }

    private fun sifreBilgisiniGuncelle() {
        val kullanici = FirebaseAuth.getInstance().currentUser
        kullanici?.updatePassword(etYeniSifre.text.toString())?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(
                    this@KullaniciAyarlarActivity,
                    "Şifreniz değiştirildi tekrar giriş yapın.",
                    Toast.LENGTH_SHORT
                ).show()
                FirebaseAuth.getInstance().signOut()
                loginSayfasınaYonlendir()
            }
        }
    }

    private fun mailAdresiniGüncelle() {
        val kullanici = FirebaseAuth.getInstance().currentUser!!
        kullanici.updateEmail(etYeniMail.text.toString()).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(
                    this@KullaniciAyarlarActivity,
                    "Mail adresi değiştirildi tekrar giriş yapın.",
                    Toast.LENGTH_SHORT
                ).show()
                FirebaseAuth.getInstance().signOut()
                loginSayfasınaYonlendir()
            } else {
                Toast.makeText(
                    this@KullaniciAyarlarActivity,
                    "Mail güncellenemedi!!!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun loginSayfasınaYonlendir() {
        var intent = Intent(this@KullaniciAyarlarActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun izinleriIste() {
        var izinler = arrayOf(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA
        )

        if (ContextCompat.checkSelfPermission(this, izinler[0]) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, izinler[1]) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, izinler[2]) == PackageManager.PERMISSION_GRANTED
        ) {
            izinlerVerildi = true
        } else {
            ActivityCompat.requestPermissions(this, izinler, 150)
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 150) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                var dialog = ProfilResmiKaydetFragment()
                dialog.show(supportFragmentManager, "fotoSec")
            } else {
                Toast.makeText(this@KullaniciAyarlarActivity, "Tüm izinleri vermelisiniz!!!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getResimYolu(resimPath: Uri?) {
        galeridenGelenUrl = resimPath

        Glide.with(ivProfilResmi).load(galeridenGelenUrl).into(ivProfilResmi)
    }

    override fun getResimBitmap(bitmap: Bitmap) {
        kameranGelenBitmap = bitmap
        ivProfilResmi.setImageBitmap(bitmap)
    }

    private fun fotografCompressed(galeridenGelenUrl: Uri) {
        var compressed = backgroundResimCompresed()
        compressed.execute(galeridenGelenUrl)
    }

    private fun fotografCompressed(kameradanGelenBitmap: Bitmap) {
        var compresed = backgroundResimCompresed(kameradanGelenBitmap)
        var uri: Uri? = null
        compresed.execute(uri)
    }

    inner class backgroundResimCompresed : AsyncTask<Uri, Double, ByteArray?> {

        var myBitmap: Bitmap? = null

        constructor() {}

        constructor(bitmap: Bitmap) {
            if (bitmap != null) {
                myBitmap = bitmap
            }
        }


        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg params: Uri?): ByteArray? {
            //galeriden resim seçilmiş
            if (myBitmap == null) {
                myBitmap = MediaStore.Images.Media.getBitmap(this@KullaniciAyarlarActivity.contentResolver, params[0])
                Log.e("TEST", "Orijinal Resin : " + (myBitmap!!.byteCount).toDouble() / MEGABYTE)
            }
            var resimByte: ByteArray? = null

            for (i in 1..5) {
                resimByte = convertToByte(myBitmap, 100 / i)
                publishProgress(resimByte!!.size.toDouble())
            }

            return resimByte
        }


        private fun convertToByte(myBitmap: Bitmap?, i: Int): ByteArray? {
            var stream = ByteArrayOutputStream()
            myBitmap?.compress(Bitmap.CompressFormat.JPEG, i, stream)

            return stream.toByteArray()
        }

        override fun onProgressUpdate(vararg values: Double?) {
            super.onProgressUpdate(*values)

        }

        override fun onPostExecute(result: ByteArray?) {
            super.onPostExecute(result)
            uploadResimToFirebase(result)
        }

    }

    private fun uploadResimToFirebase(result: ByteArray?) {
        progressGöster()
        var storageReference = FirebaseStorage.getInstance().reference
        var resimEklenecekYer =
            storageReference.child("images/users/" + FirebaseAuth.getInstance().currentUser?.uid + "/profile_resim")

        var uploadGorevi = resimEklenecekYer.putBytes(result!!)

        uploadGorevi.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            return@Continuation resimEklenecekYer.downloadUrl
        }).addOnSuccessListener {
            FirebaseDatabase.getInstance().reference.child("kullanici")
                .child(FirebaseAuth.getInstance().currentUser?.uid.toString())
                .child("profil_resmi")
                .setValue(it.toString())
            Toast.makeText(
                this@KullaniciAyarlarActivity,
                "Değişiklikler Yapıldı.",
                Toast.LENGTH_SHORT
            ).show()

            progressGizle()
        }

//        uploadGorevi.addOnSuccessListener(object : OnSuccessListener<UploadTask.TaskSnapshot> {
//            override fun onSuccess(p0: UploadTask.TaskSnapshot?) {
//                var firebaseUrl = p0?.uploadSessionUri
//                FirebaseDatabase.getInstance().reference.child("kullanici")
//                    .child(FirebaseAuth.getInstance().currentUser?.uid.toString())
//                    .child("profil_resmi")
//                    .setValue(firebaseUrl.toString())
//                Toast.makeText(
//                    this@KullaniciAyarlarActivity,
//                    "Resim Yolu : " + firebaseUrl.toString(),
//                    Toast.LENGTH_SHORT
//                ).show()
//                progressGizle()
//            }
//        })


    }

    fun progressGöster() {
        progressPicture.visibility = View.VISIBLE
    }

    fun progressGizle() {
        progressPicture.visibility = View.INVISIBLE
    }
}
