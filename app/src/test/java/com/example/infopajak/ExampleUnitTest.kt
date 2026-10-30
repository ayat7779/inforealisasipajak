package com.example.infopajak

import org.junit.Test
import org.junit.Assert.*

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testPajakApi() = kotlinx.coroutines.runBlocking {
        val okHttpClient = okhttp3.OkHttpClient.Builder()
            .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .build()
        
        val retrofit = retrofit2.Retrofit.Builder()
            .baseUrl("https://apps1.bapenda.riau.go.id/")
            .client(okHttpClient)
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
        val api = retrofit.create(com.example.infopajak.network.PajakApiService::class.java)

        val tests = listOf(
            Triple("2024-01-01", "2024-12-31", "Year 2024"),
            Triple("2025-01-01", "2025-12-31", "Year 2025"),
            Triple("2026-01-01", "2026-12-31", "Year 2026")
        )
        for ((start, end, label) in tests) {
            val req = com.example.infopajak.data.PajakRequest("bpd2026av2#", start, end)
            val res = api.getPajakData(req)
            assertEquals("Expected status 200 for $label", 200, res.status)
            assertNotNull("Expected data not null for $label", res.data)
            assertNotNull("Expected pkb not null for $label", res.data?.pkb)
            assertNotNull("Expected ap not null for $label", res.data?.ap)
            println("$label success: pkb=${res.data?.pkb}, ap=${res.data?.ap}")
        }
    }
}