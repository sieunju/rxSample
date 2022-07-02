package com.hmju.rx

import android.app.Application
import io.reactivex.rxjava3.exceptions.UndeliverableException
import io.reactivex.rxjava3.plugins.RxJavaPlugins
import java.io.IOException
import java.net.SocketException

/**
 * Description :
 *
 * Created by juhongmin on 2022/07/02
 */
class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        initRxJava()
    }

    /**
     * reactivex.exceptions.UndeliverableException 처리 함수.
     */
    private fun initRxJava() {
        RxJavaPlugins.setErrorHandler { e ->
            var error = e
            if (error is UndeliverableException) {
                error = e.cause ?: Throwable("UndeliverableException")
            }
            if (error is IOException || error is SocketException) {
                // fine, irrelevant network problem or API that throws on cancellation
                return@setErrorHandler
            }
            if (error is InterruptedException) {
                // fine, some blocking code was interrupted by a dispose call
                return@setErrorHandler
            }
            if (error is NullPointerException || error is IllegalArgumentException) {
                // that's likely a bug in the application
                Thread.currentThread().uncaughtExceptionHandler?.uncaughtException(
                    Thread.currentThread(),
                    error
                )
                return@setErrorHandler
            }
            if (error is IllegalStateException) {
                // that's a bug in RxJava or in a custom operator
                Thread.currentThread().uncaughtExceptionHandler?.uncaughtException(
                    Thread.currentThread(),
                    error
                )
                return@setErrorHandler
            }
        }
    }
}