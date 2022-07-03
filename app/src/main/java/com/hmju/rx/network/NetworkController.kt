package com.hmju.rx.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory

/**
 * Description : 간단하게 네트워크 클라이언트 처리하기위한 Class
 *
 * Created by juhongmin on 2022/07/02
 */
object NetworkController {
    const val baseURL = "https://til.qtzz.synology.me"

    val jsonFormat = Json {
        isLenient = true // Json 큰따옴표 느슨하게 체크.
        ignoreUnknownKeys = true // Field 값이 없는 경우 무시
        coerceInputValues = true // "null" 이 들어간경우 default Argument 값으로 대체
    }

    fun createHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .build()
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun createRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseURL)
            .client(createHttpClient())
            // .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.createWithScheduler(Schedulers.io()))
            .addConverterFactory(jsonFormat.asConverterFactory(MediaType.get("application/json")))
            .build()
    }

    fun createApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}
