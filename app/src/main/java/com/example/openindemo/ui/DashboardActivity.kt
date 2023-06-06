package com.example.openindemo.ui

import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.openindemo.R
import com.example.openindemo.adapters.LinkListAdapter
import com.example.openindemo.databinding.ActivityDashboardBinding
import com.example.openindemo.models.Link
import com.example.openindemo.repo.OpenInRepository
import com.example.openindemo.utils.Constants
import com.example.openindemo.utils.Resource
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.Utils
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ceil


class DashboardActivity : AppCompatActivity() {

    private val repository = OpenInRepository()

    private lateinit var binding : ActivityDashboardBinding
    private lateinit var viewModel: DashboardViewModel
    private lateinit var mAdapter : LinkListAdapter

    private var topLinkList = ArrayList<Link>()
    private var recentLinkList = ArrayList<Link>()
    private val endPoint = "v1/dashboardNew"
    private val formattedDate = ArrayList<String>()
    private val clickCount = ArrayList<Int>()
    private var urlData : Map<String,Int>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this,DashboardViewModelProviderFactory(repository))[DashboardViewModel::class.java]
        mAdapter = LinkListAdapter(this)

        setUpRecyclerView()
        initViews()
        observeViewModel()

        if(hasInternetConnection()){
            if(savedInstanceState==null){
                viewModel.getAllData(endPoint,"Bearer ${Constants.token}")
            }
        }
        else{
            binding.progressCircular.isVisible = false
            Toast.makeText(this,"Failed to load data, Check internet connection",Toast.LENGTH_SHORT).show()
        }

        setClickListener()
    }

    private fun initViews() {
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        binding.tvGreeting.text = getGreeting(currentHour)
    }

    private fun observeViewModel() {
        viewModel.userData.observe(this){resource->
            when(resource){
                is Resource.Success -> {
                    binding.progressCircular.isVisible = true
                    resource.data?.let { it ->
                        val topLinks  = it.data.top_links
                        val recentLinks = it.data.recent_links
                        topLinks.forEach { tLink ->
                            topLinkList.add(
                                Link(
                                    tLink.thumbnail,
                                    tLink.created_at,
                                    tLink.smart_link,
                                    tLink.times_ago,
                                    tLink.title,
                                    tLink.total_clicks,
                                    tLink.url_id
                                )
                            )
                        }
                        recentLinks.forEach { rLink ->
                            recentLinkList.add(
                                Link(
                                    rLink.thumbnail,
                                    rLink.created_at,
                                    rLink.smart_link,
                                    rLink.times_ago,
                                    rLink.title,
                                    rLink.total_clicks,
                                    rLink.url_id
                                )
                            )
                        }
                        urlData = it.data.overall_url_chart
                        mAdapter.differ.submitList(topLinkList)
                        binding.progressCircular.isVisible = false
                        if(urlData.isNullOrEmpty()){
                            binding.lytAnalytics.isVisible = false
                        } else {
                            binding.lytAnalytics.isVisible = true
                            setUpLineChart(urlData!!)
                        }
                    }
                }
                is Resource.Loading -> {
                    binding.progressCircular.isVisible = true
                }
                is Resource.Error -> {
                    binding.progressCircular.isVisible = false
                    Toast.makeText(this,"Failed to load data, Check your internet connection", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setClickListener() {
        binding.apply {
            radioGroup.setOnCheckedChangeListener { _, checkedId ->
                when(checkedId){
                    R.id.btnTopLink -> {
                        mAdapter.differ.submitList(topLinkList)
                    }
                    R.id.btnRecentLink -> {
                        mAdapter.differ.submitList(recentLinkList)
                    }
                }
            }
        }
    }

    private fun hasInternetConnection(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

        // For 29 api or above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork) ?: return false
            return when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ->    true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ->   true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ->   true
                else ->     false
            }
        }
        // For below 29 api
        else {
            if (connectivityManager.activeNetworkInfo != null && connectivityManager.activeNetworkInfo!!.isConnectedOrConnecting) {
                return true
            }
        }
        return false
    }

    private fun getGreeting(hour: Int): String {
        return when (hour) {
            in 0..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    private fun setUpRecyclerView() {
        binding.rvLinks.apply {
            layoutManager = LinearLayoutManager(this@DashboardActivity)
            adapter = mAdapter
        }
    }

    private fun setUpLineChart( urlData : Map<String,Int>) {

        val entries = mutableListOf<Entry>()
        val inputDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputDateFormat = SimpleDateFormat("dd MMMM", Locale.getDefault())

        var i = 0
        for((date,cCount) in urlData){
            val inputDate = inputDateFormat.parse(date)
            val outputDate = outputDateFormat.format(inputDate!!)
            formattedDate.add(outputDate)
            clickCount.add(cCount)
            entries.add(Entry(i.toFloat(),cCount.toFloat()))
            i++
        }
        val lineDataSet = LineDataSet(entries, "Clicks")

        lineDataSet.apply {
            color = ContextCompat.getColor(this@DashboardActivity, R.color.color_primary)
            lineWidth = 2f
            setDrawValues(false)
            setDrawCircles(false)
            setDrawCircleHole(false)
            setDrawFilled(true)
            if (Utils.getSDKInt() >= 18) {
                // fill drawable only supported on api level 18 and above
                val drawable = ContextCompat.getDrawable(this@DashboardActivity, R.drawable.fade_blue)
                fillDrawable = drawable
            } else {
                fillColor = Color.WHITE
            }
        }

        val lineData = LineData(lineDataSet)

        binding.apply {
            lytChart.apply {
                setTouchEnabled(false)
                setPinchZoom(false)
                legend.isEnabled = false
                description.isEnabled = false
                data = lineData

                xAxis.labelCount = 7
                xAxis.valueFormatter = MyAxisFormatter(formattedDate)
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                xAxis.setCenterAxisLabels(false)
                xAxis.granularity = 1f
                xAxis.setDrawAxisLine(true)
                xAxis.textColor = ContextCompat.getColor(this@DashboardActivity, R.color.color_grey)
                xAxis.setDrawLabels(true)
                xAxis.gridColor = ContextCompat.getColor(this@DashboardActivity, R.color.color_grey)
                xAxis.axisMinimum = 0f

                axisLeft.setDrawGridLines(true)
                axisLeft.gridColor = ContextCompat.getColor(this@DashboardActivity, R.color.color_grey)
                axisLeft.axisLineColor = ContextCompat.getColor(this@DashboardActivity, R.color.color_grey)
                axisLeft.textColor = ContextCompat.getColor(this@DashboardActivity, R.color.color_grey)
                axisLeft.axisMinimum = 0f

                axisRight.isEnabled = false
                animateXY(2000,2000)
                invalidate()
            }
            tvChartRange.text = "${formattedDate.first()} - ${formattedDate.last()}"
        }
    }

    inner class MyAxisFormatter(
        private val customLabels : List<String>
    ) : IndexAxisValueFormatter() {

        override fun getFormattedValue(value: Float): String {
            val index = ceil(value).toInt()
            return if (index >= 0 && index < customLabels.size) {
                customLabels[index]
            } else {
                ""
            }
        }
    }
}