package com.firebasekotlin.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

abstract class FCMModel {

    @Expose
    @SerializedName("data")
    var data: Data? = null
    @Expose
    @SerializedName("to")
    var to: String? = null

    class Data {
        @Expose
        @SerializedName("bildirim_turu")
        var bildirim_turu: String? = null
        @Expose
        @SerializedName("icerik")
        var icerik: String? = null
        @Expose
        @SerializedName("baslik")
        var baslik: String? = null
    }
}
