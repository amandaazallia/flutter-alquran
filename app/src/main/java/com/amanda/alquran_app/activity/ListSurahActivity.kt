package com.amanda.alquran_app.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONArrayRequestListener
import com.amanda.alquran_app.R
import com.amanda.alquran_app.fragment.FragmentScheduleSholat.Companion.newInstance
import com.amanda.alquran_app.adapter.SurahAdapter
import com.amanda.alquran_app.adapter.SurahAdapter.onSelectData
import com.amanda.alquran_app.model.Surah
import com.amanda.alquran_app.API.Urls
import com.amanda.alquran_app.utils.GetAddressIntentService
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.activity_list_surah.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class ListSurahActivity : AppCompatActivity(), onSelectData {

    var surahAdapter: SurahAdapter? = null
    var progressDialog: ProgressDialog? = null
    var modelSurah: MutableList<Surah> = ArrayList()
    var hariIni: String? = null
    var tanggal: String? = null
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var addressResultReceiver: LocationAddressResultReceiver? = null
    private var currentLocation: Location? = null
    private var locationCallback: LocationCallback? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_surah)

        progressDialog = ProgressDialog(this)
        progressDialog!!.setTitle("Mohon Tunggu")
        progressDialog!!.setCancelable(false)
        progressDialog!!.setMessage("Sedang menampilkan data...")

        addressResultReceiver = LocationAddressResultReceiver(Handler())

        val dateNow = Calendar.getInstance().time
        hariIni = DateFormat.format("EEEE", dateNow) as String
        tanggal = DateFormat.format("d MMMM yyyy", dateNow) as String
        tvToday.setText("$hariIni,")
        tvDate.setText(tanggal)

        val sendDetail = newInstance("detail")
        llTime.setOnClickListener(View.OnClickListener {
            sendDetail.show(supportFragmentManager, sendDetail.tag)
            //startActivity(new Intent(MainActivity.this, FragmentMenu.class));
        })

        llMosque.setOnClickListener(View.OnClickListener {
            startActivity(Intent(this@ListSurahActivity, MosqueLocationActivity::class.java))
        })
        rvSurah.setLayoutManager(LinearLayoutManager(this))
        rvSurah.setHasFixedSize(true)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                currentLocation = locationResult.locations[0]
                address
            }
        }
        startLocationUpdates()

        listSurah
    }

    private val listSurah: Unit
        get() {
            progressDialog!!.show()
            AndroidNetworking.get(Urls.LIST_SURAT)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onResponse(response: JSONObject) {
                        val list = response.getJSONArray("hasil");
                        for (i in 0 until list.length()) {
                            try {
                                progressDialog!!.dismiss()
                                val dataApi = Surah()

                                val jsonObject = list.getJSONObject(i)

                                dataApi.nomor = jsonObject.getString("nomor")
                                dataApi.nama = jsonObject.getString("nama")
                                dataApi.asma = jsonObject.getString("asma")
                                dataApi.ayat = jsonObject.getString("ayat")
                                dataApi.type = jsonObject.getString("type")

                                modelSurah.add(dataApi)
                                showListSurah()
                            } catch (e: JSONException) {
                                e.printStackTrace()
                                Toast.makeText(
                                    this@ListSurahActivity, "Gagal menampilkan data!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }

                    override fun onError(anError: ANError) {

                        progressDialog!!.dismiss()
                        Toast.makeText(
                            this@ListSurahActivity, "Tidak ada jaringan internet!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }

    private fun showListSurah() {
        surahAdapter = SurahAdapter(this@ListSurahActivity, modelSurah, this)
        rvSurah!!.adapter = surahAdapter
    }

    override fun onSelected(modelSurah: Surah) {
        val intent = Intent(this@ListSurahActivity, DetailSurahActivity::class.java)
        intent.putExtra("detailSurah", modelSurah)
        startActivity(intent)
    }

    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            val locationRequest = LocationRequest()
            locationRequest.interval = 1000
            locationRequest.fastestInterval = 1000
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            fusedLocationClient!!.requestLocationUpdates(locationRequest, locationCallback, null)
        }
    }

    private val address: Unit
        get() {
            if (!Geocoder.isPresent()) {
                Toast.makeText(
                    this@ListSurahActivity,
                    "Can't find current address, ",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
            val intent = Intent(this, GetAddressIntentService::class.java)
            intent.putExtra("add_receiver", addressResultReceiver)
            intent.putExtra("add_location", currentLocation)
            startService(intent)
        }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates()
            } else {
                Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private inner class LocationAddressResultReceiver internal constructor(handler: Handler?) :
        ResultReceiver(handler) {
        override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
            if (resultCode == 0) {
                address
            }
            if (resultCode == 1) {
                Toast.makeText(
                    this@ListSurahActivity,
                    "Address not found, ",
                    Toast.LENGTH_SHORT
                ).show()
            }
            val currentAdd = resultData.getString("address_result")
            showResults(currentAdd)
        }
    }

    private fun showResults(currentAdd: String?) {
        txtLocation!!.text = currentAdd
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient!!.removeLocationUpdates(locationCallback)
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 2
    }
}