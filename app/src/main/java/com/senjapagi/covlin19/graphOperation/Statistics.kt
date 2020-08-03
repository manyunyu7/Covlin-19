package com.senjapagi.covlin19.graphOperation

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.text.format.DateFormat
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.anychart.AnyChart
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.senjapagi.covlin19.util.URL
import kotlinx.android.synthetic.main.statistics.view.*
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList as ArrayList1


class Statistics(val view: View, val mContext: Context) {

    fun chartCumulatives() {
        var entriesPositiveCumulatives = ArrayList1<Entry>()
        var entriesDeathCumulatives = ArrayList1<Entry>()
        var entriesRecoveredCumulatives = ArrayList1<Entry>()
        var entriesOnHospitalCumulatives = ArrayList1<Entry>()

        var entriesNewPositives = ArrayList1<Entry>()
        var entriesNewRecovered = ArrayList1<Entry>()
        var entriesNewOnHospital = ArrayList1<Entry>()
        var entriesNewDeath = ArrayList1<Entry>()

//        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")

        AndroidNetworking.get(URL.INDONESIA_SUMMARY)
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                @SuppressLint("SetTextI18n")
                override fun onResponse(response: JSONObject?) {
                    val raz = response?.getJSONObject("update")?.getJSONArray("harian")

                    for (i in 0 until raz?.length()!!) {
                        val raz1 = response.getJSONObject("update").getJSONArray("harian")
                            .getJSONObject(i)

                        val positifCum = raz1?.getJSONObject("jumlah_positif_kum")?.getInt("value")
                        val meninggalCum =
                            raz1?.getJSONObject("jumlah_meninggal_kum")?.getInt("value")
                        val sembuhCum = raz1?.getJSONObject("jumlah_sembuh_kum")?.getInt("value")
                        val dirawatCum = raz1?.getJSONObject("jumlah_dirawat_kum")?.getInt("value")

                        val neoPositive =
                            raz1?.getJSONObject("jumlah_positif")?.getInt("value")?.toFloat()
                        val neoRecovered =
                            raz1?.getJSONObject("jumlah_sembuh")?.getInt("value")?.toFloat()
                        val neoDeath =
                            raz1?.getJSONObject("jumlah_meninggal")?.getInt("value")?.toFloat()
                        val neoOnHospital =
                            raz1?.getJSONObject("jumlah_dirawat")?.getInt("value")?.toFloat()
                        val dateUnix = raz.getJSONObject(i).getString("key").toString().toFloat()

                        entriesNewDeath.add(Entry(dateUnix, neoDeath ?: 0f))
                        entriesNewOnHospital.add(Entry(dateUnix, neoOnHospital ?: 0f))
                        entriesNewRecovered.add(Entry(dateUnix, neoRecovered ?: 0f))
                        entriesNewPositives.add(Entry(dateUnix, neoPositive ?: 0f))


                        entriesOnHospitalCumulatives.add(
                            Entry(
                                dateUnix,
                                dirawatCum?.toFloat() ?: 0f
                            )
                        )
                        entriesPositiveCumulatives.add(Entry(dateUnix, positifCum?.toFloat() ?: 0f))
                        entriesRecoveredCumulatives.add(Entry(dateUnix, sembuhCum?.toFloat() ?: 0f))
                        entriesDeathCumulatives.add(Entry(dateUnix, meninggalCum?.toFloat() ?: 0f))

                    }


                    //CUMULATIVE CASES CHART
                    view.chart.isClickable = false
                    view.chart.description.text="Penambahan Kasus"
                    view.chart.isDoubleTapToZoomEnabled = false
                    view.chart.setScaleEnabled(false)
                    view.chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
                    view.chart.animateY(1000)
                    view.chart.xAxis.valueFormatter = object :
                        ValueFormatter() {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        override fun getFormattedValue(value: Float): String {
                            val millisecond = value.toLong()
                            return DateFormat.format(
                                "dd MMM",
                                Date(millisecond)
                            ).toString()
                        }
                    }

                    val ldsPositive = (LineDataSet(entriesPositiveCumulatives, "Positif"))
                    val ldsDeath = (LineDataSet(entriesDeathCumulatives, "Meninggal"))
                    val ldsRecovered = (LineDataSet(entriesRecoveredCumulatives, "Sembuh"))
                    val ldsOnHospital = (LineDataSet(entriesOnHospitalCumulatives, "Dirawat"))

                    setupLineChart(ldsPositive, "#ffb259")
                    setupLineChart(ldsDeath, "ff5959")
                    setupLineChart(ldsRecovered, "4cd97b")
                    setupLineChart(ldsOnHospital, "9059ff")

                    val dataSets: MutableList<ILineDataSet> = java.util.ArrayList()
                    dataSets.add(ldsPositive)
                    dataSets.add(ldsDeath)
                    dataSets.add(ldsRecovered)
                    dataSets.add(ldsOnHospital)


                    val data = LineData(dataSets)
                    view.chart.data = data
                    view.chart.invalidate()


//                  NEW CASE CHART SETTINGS
                    view.newCaseChart.isClickable = false
                    view.newCaseChart.isDoubleTapToZoomEnabled = false
                    view.newCaseChart.setScaleEnabled(false)
                    view.newCaseChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
                    view.newCaseChart.xAxis.valueFormatter = object :
                        ValueFormatter() {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        override fun getFormattedValue(value: Float): String {
                            val millisecond = value.toLong()
                            return DateFormat.format(
                                "dd MMM",
                                Date(millisecond)
                            ).toString()
                        }
                    }

                    val ldsNeoPositive = (LineDataSet(entriesNewPositives, "Positif"))
                    val ldsNeoDeath = (LineDataSet(entriesNewDeath, "Meninggal"))
                    val ldsNeoRecovered = (LineDataSet(entriesNewRecovered, "Sembuh"))
                    val ldsNeoOnHospital = (LineDataSet(entriesNewOnHospital, "Dirawat"))

                    val dataSetsNewCase: MutableList<ILineDataSet> = java.util.ArrayList()
                    dataSetsNewCase.add(ldsNeoDeath)
                    dataSetsNewCase.add(ldsNeoOnHospital)
                    dataSetsNewCase.add(ldsNeoPositive)
                    dataSetsNewCase.add(ldsNeoRecovered)

                    setupLineChart(ldsNeoPositive, "#ffb259")
                    setupLineChart(ldsNeoDeath, "ff5959")
                    setupLineChart(ldsNeoRecovered, "4cd97b")
                    setupLineChart(ldsNeoOnHospital, "9059ff")


                    val newCaseData = LineData(dataSetsNewCase)
                    view.newCaseChart.data = newCaseData
                    view.newCaseChart.description.isEnabled=false
                    view.newCaseChart.invalidate()

                }

                override fun onError(anError: ANError?) {
                    makeToast("Gagal Terhubung dengan Server")
                }

            })
    }

    fun retriveSecondChart() {

        val iBarDataSets: ArrayList<IBarDataSet> = java.util.ArrayList()
        val symptomsEntry: ArrayList<String> = java.util.ArrayList()
        var symptoms: MutableList<BarEntry> = arrayListOf()

        var barEntry = BarEntry(0f, 0f)
        var barDataSet: BarDataSet = BarDataSet(symptoms, "")

        var pie = AnyChart.pie()
        val dataAge: MutableList<DataEntry> = ArrayList()

        AndroidNetworking.get(URL.DETAIL_CHART).build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    val raz = response.getJSONObject("kasus").getJSONObject("gejala")
                        .getJSONArray("list_data")
                    val age = response.getJSONObject("kasus").getJSONObject("kelompok_umur")
                        .getJSONArray("list_data")

                    for (i in 0 until age.length()) {
                        val key_age = age.getJSONObject(i).getString("key").toString()
                        val percentage = age.getJSONObject(i).getString("doc_count")
                        dataAge.add(ValueDataEntry(key_age, percentage.toFloat()))
                    }

                    pie.labels().position("outside");
                    pie.data(dataAge);
                    pie.legend().title().enabled(true)
                    pie.legend().title()
                        .text("Kelompok Usia Positif")
                        .padding(0.0, 0.0, 10.0, 0.0)
                    view.chartPieAge.setChart(pie);

                    for (i in 0 until raz.length()) {
                        val key = raz.getJSONObject(i).getString("key").toString()
                        val percentage = raz.getJSONObject(i).getString("doc_count")
                        barEntry = BarEntry(i.toFloat(), percentage.toFloat())
                        symptoms.add(barEntry)
                        symptomsEntry.add(key)
                        barDataSet = BarDataSet(symptoms, key)
                        iBarDataSets.add(barDataSet)
                        barDataSet.color = COLOR_SCHEME[i]
                    }
                    barDataSet.colors = COLOR_SCHEME
                    barDataSet.setValueTextColors(COLOR_SCHEME)
                    barDataSet.valueTextSize = 16f

                    val barData = BarData(iBarDataSets)

                    barData.setValueFormatter(object :
                        ValueFormatter() {
                        @SuppressLint("DefaultLocale")
                        override fun getFormattedValue(value: Float): String {
                            return String.format("%.1f", value) + "%"
                        }
                    })

                    view.chartPieSymtomps.data = barData
                    barData.setValueTextSize(10f)
                    view.chartPieSymtomps.data = barData
                    view.chartPieSymtomps.minimumHeight = 180
                    view.chartPieSymtomps.xAxis.setDrawLabels(false)
                    view.chartPieSymtomps.xAxis.setDrawAxisLine(false)
                    view.chartPieSymtomps.xAxis.setDrawGridLines(false)
                    view.chartPieSymtomps.axisRight.isEnabled = false
                    view.chartPieSymtomps.legend.isWordWrapEnabled = true
                    view.chartPieSymtomps.legend.textSize = 10f
                    view.chartPieSymtomps.description.isEnabled = false
                    view.chartPieSymtomps.isDoubleTapToZoomEnabled = false
                    view.chartPieSymtomps.setPinchZoom(false)
                    view.chartPieSymtomps.animateXY(1000, 1000)
                    view.chartPieSymtomps.invalidate()

                }

                override fun onError(anError: ANError?) {
                    makeToast("error nih second chart")
                }
            })

    }

    private fun setupLineChart(lineDataSet: LineDataSet, s: String) {
        lineDataSet.setDrawCircles(false)
        lineDataSet.setDrawFilled(true)
        lineDataSet.color = ColorTemplate.rgb(s)
        lineDataSet.fillColor = ColorTemplate.rgb(s)
        lineDataSet.setDrawValues(false)
        lineDataSet.axisDependency = YAxis.AxisDependency.LEFT
    }


    fun makeToast(message: String) {
        Toast.makeText(mContext, message, Toast.LENGTH_LONG).show()
    }

    private val COLOR_SCHEME = mutableListOf<Int>(
        ColorTemplate.rgb("#ffb259"),
        ColorTemplate.rgb("#ff5959"),
        ColorTemplate.rgb("4cd97b"),
        ColorTemplate.rgb("4cb5ff"),
        ColorTemplate.rgb("9059ff"),
        ColorTemplate.rgb("#ff3434"),
        ColorTemplate.rgb("#ffeeee"),
        ColorTemplate.rgb("4c9a9a"),
        ColorTemplate.rgb("4c5b5b"),
        ColorTemplate.rgb("90ff75"),
        ColorTemplate.rgb("#900407"),
        ColorTemplate.rgb("#ddddee"),
        ColorTemplate.rgb("fcfbfb"),
        ColorTemplate.rgb("000f05")
    )
}


