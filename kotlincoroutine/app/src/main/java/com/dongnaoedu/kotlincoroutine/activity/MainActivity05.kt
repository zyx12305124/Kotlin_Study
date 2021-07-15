package com.dongnaoedu.kotlincoroutine.activity

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dongnaoedu.kotlincoroutine.R
import kotlin.coroutines.*

/**
 *
 * @author ningchuanqi
 * @version V1.0
 */
class MainActivity05 : AppCompatActivity() {


    @SuppressLint("StaticFieldLeak","SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //协程体
        val continuation = suspend {
            5
        }.createCoroutine(object : Continuation<Int>{ //创建协程
            override val context: CoroutineContext = EmptyCoroutineContext //协程上下文
            override fun resumeWith(result: Result<Int>) {
                println("Coroutine End: $result")
            }
        })

        //启动协程
        continuation.resume(Unit)


        /**
         * 任务泄露
         * activity销毁了 但是网络请求还在继续
         */
    }


}