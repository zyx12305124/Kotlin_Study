package com.dongnaoedu.kotlincoroutine

import kotlinx.coroutines.*
import org.junit.Test

/**
 *
 * @author ningchuanqi
 * @version V1.0
 */
class CoroutineTest03 {

    //取消作用域会取消它的子协程
    @Test
    fun `test scope cancel 0`() = runBlocking<Unit> {
        val scope = CoroutineScope(Dispatchers.Default)
        scope.launch {
            delay(1000)
            println("job 1")
        }

        scope.launch {
            delay(1000)
            println("job 2")
        }
        //执行结果 不打印
        //原因 ： 通过CoroutineScope(Dispatchers.Default)这种方式创建的协程的作用域来创建的子协程
        //并没有继承runBlocking的上下文
        //这种方式才会打印
//        coroutineScope{
//            launch {
//                delay(1000)
//                println("job 1")
//            }
//
//            launch {
//                delay(1000)
//                println("job 2")
//            }
//        }
    }

    //取消作用域会取消它的子协程
    @Test
    fun `test scope cancel`() = runBlocking<Unit> {
        val scope = CoroutineScope(Dispatchers.Default)
        scope.launch {
            delay(1000)
            println("job 1")
        }

        scope.launch {
            delay(1000)
            println("job 2")
        }
        delay(100)
        scope.cancel()
        delay(1000)
    }

    //被取消的子协程并不会影响其余兄弟协程
    @Test
    fun `test brother job`() = runBlocking<Unit> {
        val scope = CoroutineScope(Dispatchers.Default)
        val job1 = scope.launch {
            delay(1000)
            println("job 1")
        }

        val job2 = scope.launch {
            delay(1000)
            println("job 2")
        }
        delay(100)
        job1.cancel()
        delay(1000)
    }



    @Test
    fun `test CancellationException 0`() = runBlocking<Unit> {
        val job1 = GlobalScope.launch {
            delay(1000)
            println("job 1")
        }
        //执行没有打印结果，因为这个GlobalScope有自己的作用域，这种方式去启动的协程没有继承父协程的上下文
        //所以外面的父协程不会等待里面的子协程执行结束
        //如果要等待的话就
//         job1.join()
    }

    //取消异常
    //协程通过抛出一个特殊的异常 CancellationException 来处理取消操作。
    //所有kotlinx.coroutines中的挂起函数（withContext、delay等）都是可取消的。
    @Test
    fun `test CancellationException`() = runBlocking<Unit> {
        val job1 = GlobalScope.launch {
            try {
                delay(1000)
                println("job 1")
            } catch (e: Exception) {
                e.printStackTrace() //打印 取消 llll   CancellationException$1
            }
        }
        delay(100)
        //在调用 .cancel 时您可以传入一个 CancellationException 实例来提供更多关于本次取消的详细信息
        //如果您不构建新的 CancellationException 实例将其作为参数传入的话，会创建一个默认的 CancellationException
        job1.cancel(CancellationException("取消 llll"))
        job1.join() //等待job1执行完
        //job1.cancelAndJoin() ==> cancel + join
        //我还是不不懂 cancel + join 的意义是什么
    }


    /**
     * 可以被取消的 挂起函数 withContext delay
     */
    //CPU密集型任务取消
    //isActive是一个可以被使用在CoroutineScope中的扩展属性，检查Job是否处于活跃状态。
    @Test
    fun `test cancel cpu task by isActive`() = runBlocking {
        val startTime = System.currentTimeMillis()
        //CPU密集型任务在Default调度器中运行，在主线程中通过isActive取消不了
        val job = launch(Dispatchers.Default){
            var nextPrintTime = startTime
            var i = 0
            while(i < 5 && isActive){
                //每秒打印消息两次
                if(System.currentTimeMillis() >= nextPrintTime){
                    println("job: I'm sleeping ${i++} ...")
                    nextPrintTime += 500L
                }
            }
        }
        delay(1300L)
        println("main: I'm tired of waiting!")
        job.cancelAndJoin() // 取消一个作业并且等待它结束
        //在讲job的生命周期的时候，job取消或者协程运行出错的时候任务为取消中（canceling），isActive = false,isCancelled = true;
        //当所有子协程都完成后，协程会进入已取消 Cancelled 状态，此时isCompleted = true
        //也就是说当 job.cancelAndJoin()  被调用的时候 isActive 变成了 false
        //所以这里用job.cancel + isActive 也可以达到停止cpu密集型任务的效果
        // 如`test cancel cpu task by isActive 0` 没有isActive拦截就会打印五次 无法被取消
        println("main: Now I can quit.")
    }

    @Test
    fun `test cancel cpu task by isActive 0`() = runBlocking {
        val startTime = System.currentTimeMillis()
        //CPU密集型任务在Default调度器中运行，在主线程中通过isActive取消不了
        val job = launch(Dispatchers.Default){
            var nextPrintTime = startTime
            var i = 0
            while(i < 5 ){
                //每秒打印消息两次
                if(System.currentTimeMillis() >= nextPrintTime){
                    println("job: I'm sleeping ${i++} ...")
                    nextPrintTime += 500L
                }
            }
        }
        delay(1300L)
        println("main: I'm tired of waiting!")
        job.cancelAndJoin() // 取消一个作业并且等待它结束
        println("main: Now I can quit.")
        //最后打印5次

//        问题：为什么CPU密集型任务不能被取消？ 这是协程的一个保护机制，
    }



    //ensureActive()，如果job处于非活跃状态，这个方法会立即抛出异常。
    @Test
    fun `test cancel cpu task by ensureActive`() = runBlocking {
        val startTime = System.currentTimeMillis()
        val job = launch(Dispatchers.Default){
            var nextPrintTime = startTime
            var i = 0
            while(i < 5){
                ensureActive()//点进源码 这里本质还是通过isActive去判断
                // 上面那种是悄无声息的结束了job 这种方式还会抛出一个异常告诉调用者 可以被catch到的
                if(System.currentTimeMillis() >= nextPrintTime){
                    println("job: I'm sleeping ${i++} ...")
                    nextPrintTime += 500L
                }
            }
        }
        delay(1300L)
        println("main: I'm tired of waiting!")
        job.cancelAndJoin()
        println("main: Now I can quit.")
    }



    //yield函数会检查所在协程的状态，如果已经取消，则抛出CancellationException予以响应。
    //此外，它还会尝试出让线程的执行权，给其他协程提供执行机会。
    //如果要处理的任务属于：
    //1) CPU 密集型，2) 可能会耗尽线程池资源，3) 需要在不向线程池中添加更多线程的前提下允许线程处理其他任务，那么请使用 yield()。
    @Test
    fun `test cancel cpu task by yield`() = runBlocking {
        val startTime = System.currentTimeMillis()
        val job = launch(Dispatchers.Default){
            var nextPrintTime = startTime
            var i = 0
            while(i < 5){
                yield()//出让线程执行权
                if(System.currentTimeMillis() >= nextPrintTime){
                    println("job: I'm sleeping ${i++} ...")
                    nextPrintTime += 500L
                }
            }
        }
        delay(1300L)
        println("main: I'm tired of waiting!")
        job.cancelAndJoin()
        println("main: Now I can quit.")
    }
}