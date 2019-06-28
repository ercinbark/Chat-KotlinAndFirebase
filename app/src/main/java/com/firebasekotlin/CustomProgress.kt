package com.firebasekotlin

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import com.airbnb.lottie.LottieAnimationView

class CustomProgress(context: Context) : Dialog(context) {

    lateinit var ltView: LottieAnimationView
    var cs: CustomProgress? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(R.layout.custom_progress_layout)
        ltView = findViewById(R.id.ltView)
        ltView.speed = 3F

    }


    fun getInstance(context: Context): CustomProgress {
        if (cs == null) {
            cs = CustomProgress(context)
        }
        return cs as CustomProgress
    }

    fun getProgressDialogShow() {
        cs = CustomProgress(context)
        cs!!.show()
    }


}