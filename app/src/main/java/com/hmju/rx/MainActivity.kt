package com.hmju.rx

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.hmju.rx.network.ApiService
import com.hmju.rx.network.NetworkController
import com.hmju.rx.network.model.SimpleResponse
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.schedulers.Schedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import java.util.concurrent.Callable
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private val compositeDisposable: CompositeDisposable by lazy { CompositeDisposable() }
    private val apiService: ApiService by lazy {
        NetworkController.createApiService(
            NetworkController.createRetrofit()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Do Not MainThread
        // apiService.fetchCall().execute()

        performCallType()
        performSingleType()
    }

    private fun performCallType() {
        Executors.newCachedThreadPool().execute {
            apiService.fetchCall().enqueue(object : Callback<SimpleResponse> {
                override fun onResponse(
                    call: Call<SimpleResponse>,
                    response: Response<SimpleResponse>
                ) {
                    Timber.d("onResponse $response")
                }

                override fun onFailure(call: Call<SimpleResponse>, t: Throwable) {
                    Timber.d("onFailure $t")
                }
            })
        }
    }

    private fun performSingleType() {
        apiService.fetchSingle()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Timber.d("SUCC $it")
            }, {
                Timber.d("ERROR $it")
            }).addTo(compositeDisposable)
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }
}