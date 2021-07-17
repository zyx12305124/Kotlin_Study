package com.dongnaoedu.kotlincoroutine

import android.util.Log
import kotlinx.coroutines.*
import org.junit.Test
import kotlin.system.measureTimeMillis

/**
 *
 * @author ningchuanqi
 * @version V1.0
 */
class CoroutineTest01 {

//    GlobalScope.launch{ 是一个顶级协程 一般不推荐去使用
//        GlobalScope是process级别的，只要应用不干掉，协程任务没有完成的话，这个协程都不会被结束掉
//
//    }


    //runBlocking把主线程包装成一个协程，它会等待其协程体以及所有子协程结束
    //launch与async构建器都用来启动新协程
    @Test
    fun `test coroutine builder`() = runBlocking<Unit> {
        //1.launch返回一个Job并且不附带任何结果值
        //2.async返回一个Deferred，Deferred也是一个Job，可以使用.await()在一个延期的值上得到它的最终结果
        val job1 = launch {
            delay(200)
            println("job1 finished.")
        }
        val job2 = async {
            delay(100)
            println("job2 finished.")
            "job2 result"
        }
        println(job2.await())
    }

    //等待由launch启动的一个作业
    @Test
    fun `test coroutine join`() = runBlocking<Unit> {
        val job1 = launch {
            delay(100)
            println("One")
        }
        //等job1执行完毕再启动
        job1.join()  //job1执行完毕之后 再指定job2 job3
        val job2 = launch {
            delay(50)
            println("two")
        }
        val job3 = launch {
            delay(40)
            println("three")
        }
        /**
        One
        three
        two
         */
    }

    /**
     * 通过lanuch async启动一个协程后 通过
     * job1.await()  job1.join() 来等待一个作业
     * 使用场景：等待网络请求A的结果 ，拿到A的执行结果之后
     * 再去启动后续的网络请求
     * await 和 join是挂起函数不会阻塞主线程
     */

    //等待由async启动的一个作业
    @Test
    fun `test global scope coroutine await`() = runBlocking<Unit> {
        val job1 = async {
            delay(100)
            println("One")
        }
        //等job1执行完毕再启动
        job1.await()
        val job2 = launch {
            delay(50)
            println("two")
        }
        val job3 = launch {
            delay(40)
            println("three")
        }
        /**
        One
        three
        two
         */
    }


    //不使用async
    @Test
    fun `test sync`() = runBlocking<Unit> {
        val time = measureTimeMillis {//获得这段代码执行的时间（毫秒）
            val one = doOne()
            val two = doTwo()
            println("The result:${one + two}") //The result:39
        }
        println("Completed in $time ms") //Completed in 2014 ms

    }


    private suspend fun doOne(): Int {
        delay(1000)
        return 14
    }

    private suspend fun doTwo(): Int {
        delay(1000)
        return 25
    }

    //使用async组合并发
    @Test
    fun `test combine async`() = runBlocking<Unit> {
        val time = measureTimeMillis {
            val one = async { doOne() }
            val two = async { doTwo() }
            println("The result:${one.await() + two.await()}") // The result:39

        }
        println("Completed in $time ms") // Completed in 1080 ms


        //区别在哪里？ 这样写没有起到缩短时间的作用
//            val one = async { doOne() }.await()
//            val two = async { doTwo() }.await()
//            println("The result:${one+ two}")
        //如果是  async { doOne() }.await() 和 async { doTwo() }.await()
        //则打印 Completed in 2033 ms

    }

    //协程的启动模式
    @Test
    fun `test start mode`() = runBlocking<Unit> {
        //DEFAULT：协程创建后，立即开始调度，在调度前如果协程被取消，其将直接进入取消响应的状态。
        //ATOMIC：协程创建后，立即开始调度，协程执行到第一个挂起点之前不响应取消。
        //LAZY：只有协程被需要时，包括主动调用协程的start、join或者await等函数时才会开始调度，如果调度前就被取消，那么该协程将直接进入异常结束状态。
        //UNDISPATCHED：协程创建后立即在当前函数调用栈中执行，直到遇到第一个真正挂起的点。
        //能取消吗？
        /*val job = launch(start = CoroutineStart.DEFAULT) {
            var i = 0
            while (true){
                i++
            }
            println("finished.")
        }
        delay(1000)
        job.cancel()*/

        //惰性启动
        /*val job = async (start = CoroutineStart.LAZY) {
            29
        }
        //执行一些计算
        //再启动
        job.start()*/

        //在哪个线程中执行？
        /*val job = launch(context = Dispatchers.IO, start = CoroutineStart.UNDISPATCHED) {
            println("thread:"+Thread.currentThread().name)
        }*/
    }

    @Test
    fun `test start mode default`()= runBlocking {
        //DEFAULT：协程创建后，立即开始调度，在调度前如果协程被取消，其将直接进入取消响应的状态。
        val job = launch(start = CoroutineStart.DEFAULT) {
            delay(10000)
            println("Job finished.")//如果没有job.cancel()则10秒后打印
        }
        delay(1000)
        job.cancel()
    }
}