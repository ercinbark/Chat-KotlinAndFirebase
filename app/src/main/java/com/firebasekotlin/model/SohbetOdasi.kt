package com.firebasekotlin.model

class SohbetOdasi {

    var sohbetOdasi_adi: String? = null
    var olusturan_id: String? = null
    var seviye: String? = null
    var sohbetOdasi_id: String? = null
    var sohbet_odasi_mesajlari:List<SohbetMesaj>?=null

    constructor() {}

    constructor(sohbetOdasi_adi: String, olusturan_id: String, seviye: String, sohbetOdasi_id: String,sohbet_odasi_mesajlari: List<SohbetMesaj>) {
        this.sohbetOdasi_adi = sohbetOdasi_adi
        this.olusturan_id = olusturan_id
        this.seviye = seviye
        this.sohbetOdasi_id = sohbetOdasi_id
        this.sohbet_odasi_mesajlari = sohbet_odasi_mesajlari
    }
}
