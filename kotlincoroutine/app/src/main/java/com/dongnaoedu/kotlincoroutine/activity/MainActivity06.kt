package com.dongnaoedu.kotlincoroutine.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.dongnaoedu.kotlincoroutine.R
import com.dongnaoedu.kotlincoroutine.api.userServiceApi
import kotlinx.coroutines.*
import kotlin.coroutines.*

/**
 *
 * @author ningchuanqi
 * @version V1.0
 */
//class MainActivity06 : AppCompatActivity() {
/**
 * 这里还有一种写法 mainscope可以通过一个委托的方式
 * 这里代表 MainActivity06继承接口CoroutineScope ， 但是 是通过MainScope()的方式去实现
 */
class MainActivity06 : AppCompatActivity(), CoroutineScope by MainScope() {


//    private val mainScope = MainScope()
    private var nameTextView:TextView? = null

    @SuppressLint("StaticFieldLeak","SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        nameTextView = findViewById<TextView>(R.id.nameTextView)
        nameTextView?.text = "Jack"

        val submitButton = findViewById<Button>(R.id.submitButton).also {
            it.setOnClickListener {
                //只要mainScope被取消，子协程里所有的的任务都会被取消
//                mainScope.launch {
//                    val user = userServiceApi.getUser("xx")
//                    nameTextView?.text = "address:${user?.address}"
////                    try {
////                        delay(10000) //这里延迟几秒 因为这个过程是非常快的
////                    }catch (e:Exception){
////                        e.printStackTrace()//协程被取消的时候会抛一个异常出来
////                        //JobCancellationException
////                    }
//                }


                launch { //委托
                    val user = userServiceApi.getUser("xx")
                    nameTextView?.text = "address:${user?.address}"
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //下面这两种写法都可以
        //mainScope.cancel()
        cancel() //委托
    }

    /**
     * @GET("user")
        suspend fun getUser(@Query("name") name: String) : User
        如果retrofit知道这是一个挂起函数 ， retrofit会自动的为下面这句话 开一个（异步的）协程
        val user = userServiceApi.getUser("xx")

     */


}