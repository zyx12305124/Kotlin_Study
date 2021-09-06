package com.dongnaoedu.kotlincoroutine

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Test

/**
 *
 * @author ningchuanqi
 * @version V1.0
 */
class CoroutineTest05 {

    //超时任务
    //很多情况下取消一个协程的理由是它有可能超时。
    //比如发起一个网络请求 5秒还没有响应 这个时候就干脆把任务取消掉
    @Test
    fun `test deal with timeout`() = runBlocking {
        withTimeout(1300L) {
            repeat(1000) { i ->
                println("I'm sleeping $i ...")
                delay(500L)
            }
        }
        //超时时间1300毫秒 只打印0,1,2 任务就被停止了 会抛出一个TimeoutCancellationException

//        repeat(3){//这个函数是重复执行的函数
//            println("I'm dd $it ...") //打印 0 1 2
//        }
    }

    //withTimeoutOrNull 通过返回 null 来进行超时操作，从而替代抛出一个异常
    @Test
    fun `test deal with timeout return null`() = runBlocking {
        val result = withTimeoutOrNull(1300L) {
            repeat(1000) { i ->     // 如果超时了，result则为null
//            repeat(2) { i ->            // 如果在超时时间内完成任务(未超时) ，result打印展示危Done
                println("I'm sleeping $i ...")
                delay(500L)
            }
            "Done" // 在它运行得到结果之前取消它
        }
//        }?:"为空"
        println("Result is $result")

    }

}