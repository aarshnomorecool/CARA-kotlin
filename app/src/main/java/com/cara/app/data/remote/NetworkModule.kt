package com.cara.app.data.remote

import com.cara.app.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

// BASE_URL comes from BuildConfig (set per build type in app/build.gradle.kts)
// so dev (emulator -> localhost backend) and release (deployed backend) can
// differ without touching source, per CLAUDE_android.md's "no hardcoded
// backend URLs" rule.
object NetworkModule {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .apply {
            if (BuildConfig.DEBUG) {
                // The dev backend is often reached over `adb reverse`
                // tunneled through USB, which is slower and less
                // consistent than a normal network - the default 10s
                // timeout was causing real requests to fail and fall back
                // to the offline cache unnecessarily. Debug-only; release
                // keeps OkHttp's normal defaults against the real deployed
                // backend.
                connectTimeout(30, TimeUnit.SECONDS)
                readTimeout(30, TimeUnit.SECONDS)
                writeTimeout(30, TimeUnit.SECONDS)
            }
        }
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
