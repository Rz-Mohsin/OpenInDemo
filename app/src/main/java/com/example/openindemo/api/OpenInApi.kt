package com.example.openindemo.api

import com.example.openindemo.models.OpenInApiResponse
import retrofit2.Response
import retrofit2.http.GET

interface OpenInApi {

    @GET("v1/dashboardNew")
    suspend fun fetchData() : Response<OpenInApiResponse>

}