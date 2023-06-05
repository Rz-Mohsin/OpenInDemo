package com.example.openindemo.repo

import com.example.openindemo.api.RetrofitInstance

class OpenInRepository {

    suspend fun getAllData(endPoint : String, token : String) =
        RetrofitInstance.api.fetchData(endPoint,token)

}