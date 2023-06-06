package com.example.openindemo.api

import com.example.openindemo.models.OpenInApiResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Url

interface OpenInApi {

    @GET
    suspend fun fetchData(
        @Url
        endpoint: String,
        @Header
        ("Authorization") token: String
    )
    : Response<OpenInApiResponse>

}