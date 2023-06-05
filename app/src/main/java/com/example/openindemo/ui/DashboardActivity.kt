package com.example.openindemo.ui

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
import java.util.*
import kotlin.collections.ArrayList

class DashboardActivity : AppCompatActivity() {

    private val repository = OpenInRepository()

    private lateinit var binding : ActivityDashboardBinding
    private lateinit var viewModel: DashboardViewModel
    private lateinit var madapter : LinkListAdapter
    private var topLinkList = ArrayList<Link>()
    private var recentLinkList = ArrayList<Link>()
    private val endPoint = "v1/dashboardNew"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this,DashboardViewModelProviderFactory(repository))[DashboardViewModel::class.java]
        madapter = LinkListAdapter(this)
        setUpRecyclerView()
        initViews()

        if(hasInternetConnection()){
            viewModel.getAllData(endPoint,"Bearer ${Constants.token}")
        }
        else{
            Toast.makeText(this,"Failed to load data, Check internet connection",Toast.LENGTH_SHORT).show()
        }
        observeViewModel()
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
                    resource.data?.let { it ->
                        Log.d("error001","Fetched data $it")
                        val topLinks  = it.data.top_links
                        val recentLinks = it.data.recent_links
                        topLinks.forEach {
                            topLinkList.add(
                                Link(
                                    it.thumbnail,
                                    it.created_at,
                                    it.smart_link,
                                    it.times_ago,
                                    it.title,
                                    it.total_clicks,
                                    it.url_id
                                )
                            )
                        }
                        recentLinks.forEach {
                            recentLinkList.add(
                                Link(
                                    it.thumbnail,
                                    it.created_at,
                                    it.smart_link,
                                    it.times_ago,
                                    it.title,
                                    it.total_clicks,
                                    it.url_id
                                )
                            )
                        }
                        madapter.differ.submitList(topLinkList)
                        binding.progressCircular.isVisible = false
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
                        Log.d("error001","Button Top link clicked")
                        madapter.differ.submitList(topLinkList)
                    }
                    R.id.btnRecentLink -> {
                        Log.d("error001","Button Recent link clicked")
                        madapter.differ.submitList(recentLinkList)
                    }
                }
            }
        }
    }

    private fun hasInternetConnection(): Boolean {
        val connectivityManager = getSystemService(
            CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            return when{
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        }
        else
        {
            connectivityManager.activeNetworkInfo?.run {
                return when(type){
                    ConnectivityManager.TYPE_WIFI -> true
                    ConnectivityManager.TYPE_MOBILE -> true
                    ConnectivityManager.TYPE_ETHERNET -> true
                    else -> false
                }
            }
        }
        return false
    }

    private fun getGreeting(hour: Int): String {
        return when (hour) {
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    private fun setUpRecyclerView() {
        binding.rvLinks.apply {
            layoutManager = LinearLayoutManager(this@DashboardActivity)
            adapter = madapter
        }
    }
}