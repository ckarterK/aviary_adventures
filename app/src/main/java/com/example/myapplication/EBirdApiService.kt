package com.example.myapplication

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query
interface EBirdApiService {
    //adapted from youtube
//    authour:CodingZest
//    link to Author:https://www.youtube.com/@codingwithmitch
//    link:https://www.youtube.com/@CodingZest
//    date:20 Sept 2018
    @Headers("X-eBirdApiToken: u24mdfjitqe7")
    @GET("v2/data/obs/{country}/recent")
    fun getRecentHotspots(
        @Path("country") country:String
    ): Call<List<HotspotsItem>>
}