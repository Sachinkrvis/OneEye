package com.example.vision2.modules

import com.example.vision2.dataClass.userLocation
import retrofit2.http.GET
import retrofit2.Response

interface LocationCall {

    @GET("/addresses")
    suspend fun getLocation(): Response<List<userLocation>>
}