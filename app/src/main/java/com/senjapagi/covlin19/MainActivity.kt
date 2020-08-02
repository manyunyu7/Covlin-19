package com.senjapagi.covlin19

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils.loadAnimation
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_ai.*
import kotlinx.android.synthetic.main.layout_ai.btnCloseAI
import kotlinx.android.synthetic.main.layout_loading_transparent.*
import kotlinx.android.synthetic.main.statistics.*
import org.json.JSONObject
import java.security.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {
    //AUTHOR : HENRY AUGUSTA , SYSTEM ENGINEERING - TELKOM UNIVERSITY
    val calendar = Calendar.getInstance()
    lateinit var dataProvince: MutableMap<String, Int>
    lateinit var indexLocalProvince: MutableMap<String, Int>
    var provinceCase = ArrayList<ModelProvince>()
    var provinceSpinnerData = ArrayList<String>()
    var tempIDProv = 0
    var localPointerProv = 0


    //entry for chart 1
    var entries = ArrayList<Entry>()

    override fun onResume() {
        super.onResume()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataProvince = mutableMapOf()
        indexLocalProvince = mutableMapOf()
        setContentView(R.layout.activity_main)

        lineChart1()

        btnOpenAI.setOnClickListener {
            lyt_prixa.visibility = View.VISIBLE
            lyt_prixa.animation = loadAnimation(this, R.anim.item_animation_appear_bottom)
        }

        btnCloseAI.setOnClickListener {
            lyt_prixa.visibility = View.GONE
            lyt_prixa.animation = loadAnimation(this, R.anim.item_animation_gone_bottom)
        }

        a3.setOnClickListener {
            lyt_statistics.visibility = View.VISIBLE
            lyt_statistics.animation = loadAnimation(this, R.anim.item_animation_appear_bottom)
        }

        btnCloseStatistic.setOnClickListener {
            lyt_statistics.visibility = View.GONE
            lyt_statistics.animation = loadAnimation(this, R.anim.item_animation_gone_bottom)
        }


        btnCall.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:119")
            startActivity(intent)
        }

        //PRIXA.AI
        val webSettings = webView.settings
        webView.settings.domStorageEnabled = true
        webSettings.javaScriptEnabled = true
        webSettings.allowFileAccess = true
        webSettings.allowContentAccess = true
        webView.clearCache(true)
        webView.loadUrl(URL.PRIXA_AI)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                loading_indicator.visibility = View.INVISIBLE
                webView.visibility = View.VISIBLE
            }

            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                loading_indicator.visibility = View.VISIBLE
            }

            override fun onReceivedError(
                view: WebView,
                errorCod: Int,
                description: String,
                failingUrl: String
            ) {
                makeToast("Your Internet Connection May not be active Or $description")
                loading_indicator.visibility = View.VISIBLE
            }

        }
        a2.text = "Updated at " + SimpleDateFormat("dd-MM-yyyy", Locale.US).format(calendar.time)
            .toString()
        getProvince()
        getSummary()
        spinnerProvince.setOnItemSelectedListener { view, position, id, item ->
            tempIDProv = dataProvince[item] ?: 0
            localPointerProv = indexLocalProvince[item] ?: 0
            makeToast("$item")
            if (tempIDProv == 0) {
                infected_new.visibility = View.VISIBLE
                recovered_new.visibility = View.VISIBLE
                death_new.visibility = View.VISIBLE
                in_hospital.visibility = View.VISIBLE
                odp.visibility = View.VISIBLE
                negative.visibility = View.VISIBLE
                infected.text = "..."
                death.text = "..."
                recovered.text = "..."
                getSummary()
            } else {
                infected_new.visibility = View.INVISIBLE
                recovered_new.visibility = View.INVISIBLE
                death_new.visibility = View.INVISIBLE
                in_hospital.text = "Dirawat : -"
                odp.text = "Total ODP : -"
                negative.text = "Spesimen Negaif : -"
                infected.text = provinceCase[localPointerProv].positive.toString()
                death.text = provinceCase[localPointerProv].death.toString()
                recovered.text = provinceCase[localPointerProv].healed.toString()
            }
        }
    }

    fun getSummary() {
        AndroidNetworking.get(URL.INDONESIA_SUMMARY)
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                @SuppressLint("SetTextI18n")
                override fun onResponse(response: JSONObject?) {
                    val raz = response?.getJSONObject("data")
                    val ky = response?.getJSONObject("update")?.getJSONObject("total")
                    val feb = response?.getJSONObject("update")?.getJSONObject("penambahan")


                    val newDeathString = "+ ${feb?.getString("jumlah_meninggal")}"
                    val newHealedString = "+ ${feb?.getString("jumlah_sembuh")}"
                    val newPositive = "+ ${feb?.getString("jumlah_positif")}"

                    a2.text = "Updated at " + feb?.getString("created")

                    death_new.text = newDeathString
                    recovered_new.text = newHealedString
                    infected_new.text = newPositive


                    val odpString = "Total ODP : " + raz?.getString("jumlah_odp")
                    val negativeString =
                        "Spesimen Negatif : " + raz?.getString("total_spesimen_negatif")
                    val onHospitalString = "Dirawat : " + ky?.getString("jumlah_dirawat")

                    odp.text = odpString
                    negative.text = negativeString
                    in_hospital.text = onHospitalString

                    infected.text = ky?.getString("jumlah_positif")
                    death.text = ky?.getString("jumlah_meninggal")
                    recovered.text = ky?.getString("jumlah_sembuh")
                    getProvince()
                }

                override fun onError(anError: ANError?) {
                    makeToast("Gagal Terhubung dengan Server")
                }

            })
    }

    fun getProvince() {
        //CLEARING ALL DATA BEFORE RETRIEVING MOST UPDATED DATA
        provinceSpinnerData.clear()
        indexLocalProvince.clear()
        provinceCase.clear()
        dataProvince.clear()
        provinceSpinnerData.add("Seluruh Indonesia")
        AndroidNetworking.get(URL._PROVINCE)
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    val raz = response?.getJSONArray("data")
                    val razLength = raz?.length()
                    for (i in 0 until razLength!!) {
                        val kodeProvince = raz.getJSONObject(i).getInt("kodeProvi")
                        val nameProvince = raz.getJSONObject(i).getString("provinsi")
                        val positive = raz.getJSONObject(i).getInt("kasusPosi")
                        val death = raz.getJSONObject(i).getInt("kasusMeni")
                        val healed = raz.getJSONObject(i).getInt("kasusSemb")
                        dataProvince.put(nameProvince, kodeProvince)
                        indexLocalProvince.put(nameProvince, i)
                        provinceSpinnerData.add(nameProvince)
                        provinceCase.add(
                            ModelProvince(
                                id = kodeProvince,
                                name = nameProvince,
                                death = death,
                                positive = positive,
                                healed = healed
                            )
                        )
                    }
                    spinnerProvince.setItems(provinceSpinnerData)
                }

                override fun onError(anError: ANError?) {
                    makeToast("Gagal Terhubung dengan Server")
                }

            })
    }

    fun lineChart1() {

        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")

        AndroidNetworking.get(URL.INDONESIA_SUMMARY)
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                @SuppressLint("SetTextI18n")
                override fun onResponse(response: JSONObject?) {
                    val raz = response?.getJSONObject("update")?.getJSONArray("harian")
                    for (i in 0..raz?.length()!!) {
                        val positif_kum =
                            raz.getJSONObject(i).getJSONObject("jumlah_positif_kum").getInt("value")
                        val dateUnix = raz.getJSONObject(i).getString("key_as_string")
                    }

                }

                override fun onError(anError: ANError?) {
                    makeToast("Gagal Terhubung dengan Server")
                }

            })
    }

    fun makeToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun convertUnixTime(epoc: Long): String? {
        try {
            val netDate = Date(epoc * 1000)
            return netDate.toString()
        } catch (e: Exception) {
            return e.toString()
        }
    }
}