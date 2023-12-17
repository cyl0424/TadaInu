package com.mobileprogramming.tadainu.FCMmanagement

import com.mobileprogramming.tadainu.FCMmanagement.Constants.Companion.FCM_URL
import com.mobileprogramming.tadainu.MainActivity
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.FileInputStream
import java.io.IOException
import java.util.Properties


object RetrofitInstance {

    private val retrofit by lazy {

        Retrofit.Builder()
            .baseUrl(FCM_URL)
            .client(provideOkHttpClient(AppInterceptor()))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api : FcmInterface by lazy {
        retrofit.create(FcmInterface::class.java)
    }

    // Client
    private fun provideOkHttpClient(
        interceptor: AppInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .run {
            addInterceptor(interceptor)
            build()
        }

    // 헤더 추가
    class AppInterceptor : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain)
                : Response = with(chain) {

            // local.properties 파일 경로
            val propertiesFilePath = "local.properties"

            // Properties 객체 생성
            val properties = Properties()

            var serverKey = ""

            try {
                // 파일에서 속성 읽기
                FileInputStream(propertiesFilePath).use { fileInput ->
                    properties.load(fileInput)
                }

                // 원하는 속성 가져오기
                serverKey = properties.getProperty("serverKsy")

                // 다른 속성 가져오기 예제
                // val anotherProperty = properties.getProperty("another.property.key")
                // println("Another Property: $anotherProperty")

            } catch (e: Exception) {
                e.printStackTrace()
            }

            val newRequest = request().newBuilder()
                .addHeader("Authorization", "key=${serverKey}")
                .addHeader("Content-Type", "application/json")
                .build()
            proceed(newRequest)
        }
    }
}