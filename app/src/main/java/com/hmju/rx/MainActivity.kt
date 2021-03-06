package com.hmju.rx

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.hmju.rx.network.ApiService
import com.hmju.rx.network.NetworkController
import com.hmju.rx.network.model.SimplePayload
import com.hmju.rx.network.model.SimpleResponse
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.*
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.Action
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.processors.BehaviorProcessor
import io.reactivex.rxjava3.processors.PublishProcessor
import io.reactivex.rxjava3.schedulers.Schedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import kotlin.random.nextInt

class MainActivity : AppCompatActivity() {

    private val compositeDisposable: CompositeDisposable by lazy { CompositeDisposable() }
    private val apiService: ApiService by lazy {
        NetworkController.createApiService(
            NetworkController.createRetrofit()
        )
    }

    private val tvTitle: TextView by lazy { findViewById(R.id.tvTitle) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Do Not MainThread
        // apiService.fetchCall().execute()

        // performCallType()
        performNetworkSingleType()

        // singleSimpleExample()
        // maybeSingleExample()
        exampleHotObservable()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }

    private fun performCallType() {
        Executors.newCachedThreadPool().submit {
            apiService.fetchCall().enqueue(object : Callback<SimpleResponse> {
                override fun onResponse(
                    call: Call<SimpleResponse>,
                    response: Response<SimpleResponse>
                ) {
                    // UI Thread
                    val payload = toPayload(response.body())
                    tvTitle.text = payload.toString()
                }

                override fun onFailure(call: Call<SimpleResponse>, t: Throwable) {
                    Timber.d("onFailure $t")
                }
            })
        }
    }

    @Throws(NullPointerException::class)
    private fun toPayload(res: SimpleResponse?): SimplePayload {
        return res?.data?.payload ?: throw NullPointerException("payload is Null")
    }

    private fun performNetworkSingleType() {
        apiService.fetchSingle()
            .map { toPayload(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                // API ???????????? toPayload ??? ????????? ?????????????????? Cache Thread
                // Current UI Thread
                tvTitle.text = it.toString()
            }, {
                Timber.d("ERROR $it")
            }).addTo(compositeDisposable)
    }

    private fun singleSimpleExample() {
        Single.just(System.currentTimeMillis())
            .subscribe(object : SingleObserver<Long> {
                override fun onSubscribe(d: Disposable) {
                    d.addTo(compositeDisposable)
                }

                override fun onSuccess(t: Long) {
                    Timber.d("onSuccess $t")
                }

                override fun onError(e: Throwable) {
                    Timber.d("onError $e")
                }
            })

        Single.create<Long> { emitter ->
            if (Random.nextBoolean()) {
                emitter.onSuccess(System.currentTimeMillis())
            } else {
                emitter.onError(RuntimeException("Sample Error"))
            }
        }.subscribe(object : Consumer<Long> {
            override fun accept(t: Long) {
                Timber.d("SUCC $t")
            }
        }, object : Consumer<Throwable> {
            override fun accept(t: Throwable) {
                Timber.d("ERROR")
            }
        }).addTo(compositeDisposable)
    }

    private fun maybeSingleExample() {
        Maybe.just(System.currentTimeMillis())
            .subscribe(object : Consumer<Long> {
                override fun accept(t: Long) {
                    Timber.d("SUCC $t")
                }
            }, object : Consumer<Throwable> {
                override fun accept(t: Throwable) {
                    Timber.d("ERROR")
                }
            }, object : Action {
                override fun run() {
                    Timber.d("onCompleted")
                }
            }).addTo(compositeDisposable)

        Maybe.create<Long> { emitter ->
            val ran = Random.nextInt(0 until 10)
            if (ran < 3) {
                emitter.onSuccess(System.currentTimeMillis())
            } else if (ran in 3..5) {
                emitter.onError(RuntimeException("Sample Error"))
            } else {
                emitter.onComplete()
            }
        }.subscribe(object : Consumer<Long> {
            override fun accept(t: Long) {
                Timber.d("SUCC $t")
            }
        }, object : Consumer<Throwable> {
            override fun accept(t: Throwable) {
                Timber.d("ERROR")
            }
        }, object : Action {
            override fun run() {
                Timber.d("onCompleted")
            }
        }).addTo(compositeDisposable)
    }

    private fun exampleFlowableBackPress() {
        Flowable.interval(100, TimeUnit.MILLISECONDS)
            .onBackpressureBuffer()
            .subscribe({
                // ???????????? ????????? 500ms ????????? ?????? ?????? ????????? ????????????.
            }, {

            }).addTo(compositeDisposable)
    }

    private fun exampleFlowable1() {
        Flowable.just(System.currentTimeMillis())
            .subscribe(object : Consumer<Long> {
                override fun accept(t: Long) {
                    Timber.d("SUCC $t")
                }
            }, object : Consumer<Throwable> {
                override fun accept(t: Throwable) {
                    Timber.d("ERROR")
                }
            }, object : Action {
                override fun run() {
                    Timber.d("onCompleted")
                }
            }).addTo(compositeDisposable)

        Flowable.create<Long>(object : FlowableOnSubscribe<Long> {
            override fun subscribe(emitter: FlowableEmitter<Long>) {
                val ran = Random.nextInt(0 until 10)
                if (ran < 3) {
                    emitter.onNext(System.currentTimeMillis())
                } else if (ran in 3..5) {
                    emitter.onError(RuntimeException("Sample Error"))
                } else {
                    emitter.onComplete()
                }
            }
        }, BackpressureStrategy.BUFFER)
            .subscribe(object : Consumer<Long> {
                override fun accept(t: Long) {
                    Timber.d("SUCC $t")
                }
            }, object : Consumer<Throwable> {
                override fun accept(t: Throwable) {
                    Timber.d("ERROR")
                }
            }, object : Action {
                override fun run() {
                    Timber.d("onCompleted")
                }
            }).addTo(compositeDisposable)
    }

    private fun exampleColdObservable() {
        Flowable.interval(1000, TimeUnit.MILLISECONDS)
            .onBackpressureBuffer()
            .subscribe({
                // Do Working
            }, {

            }).addTo(compositeDisposable)
    }

    private val behavior = BehaviorProcessor.create<Long>()
    private val publisher = PublishProcessor.create<Long>()

    private fun exampleHotObservable() {
        publisher.subscribe({
            Timber.d("One Sub $it")
        }, {

        })
        publisher.onNext(System.currentTimeMillis())
        publisher.subscribe({
            Timber.d("Two Sub $it")
        }, {

        })
    }

    private fun exampleOne() {
        apiService.postLogin()
            .subscribe({
                // ????????? ?????? ??? ???????????? ?????? ??????
                if (it.status) {
                    fetchUserLike()
                }
            }, {

            }).addTo(compositeDisposable)

        apiService.postLogin()
            .flatMap {
                if (it.status) {
                    apiService.fetchUserLike()
                } else {
                    throw NullPointerException("Login is Fail")
                }
            }.subscribe({ list ->
                // ???????????? ?????????..
            }, {

            }).addTo(compositeDisposable)
    }

    private fun fetchUserLike() {
        apiService.fetchUserLike().subscribe({}, {}).addTo(compositeDisposable)
    }

    private fun start() {
        Single.merge(?????????(), ????????????(), ??????????????????())
            .observeOn(AndroidSchedulers.mainThread())
            .buffer(3)
            .subscribe({ list ->
                // buffer ????????? ?????? ????????? ??????????????? ???????????? List ??????????????? ???????????? ????????????.
            }, {

            }).addTo(compositeDisposable)
    }

    private fun ?????????(): Single<String> {
        return Single.just("????????? API ???????????? ???????????? ???????????????.")
            .onErrorReturn { "ddd" }
            .subscribeOn(Schedulers.io())
    }

    private fun ????????????(): Single<String> {
        return Single.just("?????? ?????? API ???????????? ???????????? ???????????????.").subscribeOn(Schedulers.io())
    }

    private fun ??????????????????(): Single<String> {
        return Single.just("?????????????????? API ???????????? ???????????? ???????????????.").subscribeOn(Schedulers.io())
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }
}