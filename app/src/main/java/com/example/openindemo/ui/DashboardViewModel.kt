package com.example.openindemo.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.openindemo.models.OpenInApiResponse
import com.example.openindemo.repo.OpenInRepository
import com.example.openindemo.utils.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response

class DashboardViewModel(
    val repository: OpenInRepository
) : ViewModel() {

    private val _userData = MutableLiveData<Resource<OpenInApiResponse>>()
    val userData : LiveData<Resource<OpenInApiResponse>>
    get() = _userData

    fun getAllData(endPoint : String, token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            _userData.postValue(Resource.Loading())
            try{
                val response = repository.getAllData(endPoint,token)
                _userData.postValue(handleResponse(response))
            } catch (e : Exception) {
                _userData.postValue(Resource.Error("Failed to load data, Check your internet connection"))
            }
        }
    }

    private fun <T> handleResponse(response: Response<T>) : Resource<T> {
        if(response.isSuccessful) {
            response.body()?.let {
                return Resource.Success(it)
            }
        }
        return Resource.Error(response.message())
    }

}