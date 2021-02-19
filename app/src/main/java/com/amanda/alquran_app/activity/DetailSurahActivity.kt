package com.amanda.alquran_app.activity

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Html
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.amanda.alquran_app.API.Urls
import com.amanda.alquran_app.R
import com.amanda.alquran_app.adapter.AyatAdapter
import com.amanda.alquran_app.model.Ayat
import com.amanda.alquran_app.model.Surah
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONArrayRequestListener
import kotlinx.android.synthetic.main.activity_detail_surah.*
import org.json.JSONArray
import org.json.JSONException
import java.io.IOException
import java.util.ArrayList

class DetailSurahActivity  : AppCompatActivity() {

    var nomor: String? = null
    var nama: String? = null
    var arti: String? = null
    var type: String? = null
    var ayat: String? = null
    var modelSurah: Surah? = null
    var ayatAdapter: AyatAdapter? = null
    var progressDialog: ProgressDialog? = null
    var modelAyat: MutableList<Ayat> = ArrayList()
    var mHandler: Handler? = null

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_surah)

        toolbar_detail.setTitle(null)
        setSupportActionBar(toolbar_detail)
        assert(supportActionBar != null)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        mHandler = Handler()

        modelSurah = intent.getSerializableExtra("detailSurah") as Surah
        if (modelSurah != null) {
            nomor = modelSurah!!.nomor
            nama = modelSurah!!.nama
            type = modelSurah!!.type
            ayat = modelSurah!!.ayat

            fabStop.setVisibility(View.GONE)
            fabPlay.setVisibility(View.VISIBLE)

            tvHeader.setText(nama)
            tvTitle.setText(nama)
            tvSubTitle.setText(arti)
            tvInfo.setText("$type - $ayat Ayat ")

        }

        progressDialog = ProgressDialog(this)
        progressDialog!!.setTitle("Mohon Tunggu")
        progressDialog!!.setCancelable(false)
        progressDialog!!.setMessage("Sedang menampilkan data...")

        rvAyat.setLayoutManager(LinearLayoutManager(this))
        rvAyat.setHasFixedSize(true)

        for (x in 0 until 10) println(x)
//        listAyat

    }
//
//    private val listAyat: Unit
//        get() {
//            progressDialog!!.show()
//            AndroidNetworking.get(Urls.DETAIL_SURAT)
//                .addPathParameter("nomor", nomor)
//                .setPriority(Priority.MEDIUM)
//                .build()
//                .getAsJSONArray(object : JSONArrayRequestListener {
//                    override fun onResponse(response: JSONArray) {
//                        for (i in 0 until response.length()) {
//                            try {
//                                progressDialog!!.dismiss()
//                                val dataApi = Ayat()
//                                val jsonObject = response.getJSONObject(i)
//                                dataApi.nomor = jsonObject.getString("nomor")
//                                dataApi.arab = jsonObject.getString("ar")
//                                dataApi.indo = jsonObject.getString("id")
//                                dataApi.terjemahan = jsonObject.getString("idt")
//                                modelAyat.add(dataApi)
//                                showListAyat()
//                            } catch (e: JSONException) {
//                                e.printStackTrace()
//                                Toast.makeText(this@DetailSurahActivity, "Gagal menampilkan data!",
//                                    Toast.LENGTH_SHORT).show()
//                            }
//                        }
//                    }
//                    override fun onError(anError: ANError) {
//                        progressDialog!!.dismiss()
//                        Toast.makeText(this@DetailSurahActivity, "Tidak ada jaringan internet!",
//                            Toast.LENGTH_SHORT).show()
//                    }
//                })
//        }
//
//    private fun showListAyat() {
//        ayatAdapter = AyatAdapter(this@DetailSurahActivity, modelAyat)
//        rvAyat!!.adapter = ayatAdapter
//    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}