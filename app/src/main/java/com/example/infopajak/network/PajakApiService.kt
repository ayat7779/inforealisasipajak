package com.example.infopajak.network

import com.example.infopajak.data.PajakRequest
import com.example.infopajak.data.PajakResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface PajakApiService {
    @POST("apiv2/matapajak_range_tanggal")
    suspend fun getPajakData(@Body request: PajakRequest): PajakResponse
}
