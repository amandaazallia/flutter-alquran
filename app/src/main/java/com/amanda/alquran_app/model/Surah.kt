package com.amanda.alquran_app.model

import java.io.Serializable

class Surah : Serializable {

    @JvmField
    var nomor: String? = null

    @JvmField
    var nama: String? = null

    @JvmField
    var asma: String? = null

    @JvmField
    var ayat: String? = null

    @JvmField
    var type: String? = null
}