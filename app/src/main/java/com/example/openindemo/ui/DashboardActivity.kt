package com.example.openindemo.ui

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.openindemo.R
import com.example.openindemo.databinding.ActivityDashboardBinding
import com.example.openindemo.repo.OpenInRepository
import com.example.openindemo.utils.Constants
import com.example.openindemo.utils.Resource

class DashboardActivity : AppCompatActivity() {

    private val repository = OpenInRepository()

    private lateinit var binding : ActivityDashboardBinding
    private lateinit var viewModel: DashboardViewModel
    private val endPoint = "v1/dashboardNew"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //initialising viewModel
        viewModel = ViewModelProvider(this,DashboardViewModelProviderFactory(repository))[DashboardViewModel::class.java]

        //fetching data when app opens
        if(hasInternetConnection()){
            viewModel.getAllData(endPoint,"Bearer ${Constants.token}")
        }
        else{
            Toast.makeText(this,"Failed to load data, Check internet connection",Toast.LENGTH_SHORT).show()
        }
        observeViewModel()
        setClickListener()
    }

    //Setting observer
    private fun observeViewModel() {
        viewModel.userData.observe(this){resource->
            when(resource){
                is Resource.Success -> {
                    resource.data?.let { it ->
                        Log.d("error001","Fetched data $it")
                    }

                }
                is Resource.Loading -> {

                }
                is Resource.Error -> {
                    Toast.makeText(this,"Failed to load data, Check your internet connection", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    //Setting click listener
    private fun setClickListener() {
        binding.apply {
            radioGroup.setOnCheckedChangeListener { _, checkedId ->
                when(checkedId){
                    R.id.btnTopLink -> {
                        Log.d("error001","Button Top link clicked")
                    }
                    R.id.btnRecentLink -> {
                        Log.d("error001","Button Recent link clicked")
                    }
                }
            }
        }
    }

    //To check internet permission
    private fun hasInternetConnection(): Boolean{
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
}