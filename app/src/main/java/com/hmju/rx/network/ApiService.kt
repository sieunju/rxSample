package com.hmju.rx.network

import com.hmju.rx.network.model.SimplePayload
import com.hmju.rx.network.model.SimpleResponse
import io.reactivex.rxjava3.core.Single
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Description : 간단한 API 서비스
 *
 * Created by juhongmin on 2022/07/02
 */
interface ApiService {
    @GET("/api/jsend")
    fun fetchSingle(): Single<SimpleResponse>

    @GET("/api/jsend")
    fun fetchCall(): Call<SimpleResponse>

    @POST("/api/login")
    fun postLogin() : Single<SimpleResponse>

    @GET("/api/user/like")
    fun fetchUserLike() : Single<List<SimplePayload>>
}