package com.dongnaoedu.kotlincoroutineexception

import kotlinx.coroutines.*
import org.junit.Test
import java.io.IOException
import kotlin.AssertionError

/**
 *
 * @author ningchuanqi
 * @version V1.0
 */
class CoroutineTest01 {

    @Test
    fun `test CoroutineContext`() = runBlocking<Unit> {
        launch(Dispatchers.Default + CoroutineName("terrrst")) {
            println("I'm working in thread ${Thread.currentThread().name}")
        }
        //这里的coroutineContext指定了两个元素
        //Dispatchers.Default 调度器
        //CoroutineName("test") 指定协程的名字
        //为什么可以相加
        //因为CoroutineContext类里面进行了运算符的重载
    }

    @Test
    fun `test CoroutineContext extend`() = runBlocking<Unit> {

        //这段代码的意思是 首先定义了一个协程作用域 CoroutineScope
        //在协程作用域中 通过lanuch构建器来启动一个协程
        //然后在lanuch里面通过async 来启动一个launch的子协程
        //最后打印结果 两次打印job对象不一样 但协程名是一样的
        //DefaultDispatcher-worker-2 是调度器

        val scope = CoroutineScope(Job() + Dispatchers.IO + CoroutineName("te666st"))
//        val scope = CoroutineScope(Job() + Dispatchers.IO )
        val job = scope.launch {
            println("打印一 ： ${coroutineContext[Job]}  ${Thread.currentThread().name}")
            val result = async {
                println("打印二 ： ${coroutineContext[Job]}  ${Thread.currentThread().name}")
                delay(1000)
                "async OK"
            }.await()
            println("打印三 ： $result")
        }
        job.join()
    }

    @Test
    fun `test CoroutineContext extend 0`() = runBlocking<Unit> {
        val job1 = Job()
        println("job1打印 $job1")// job1打印 JobImpl{Active}@11531931
        val scope = CoroutineScope(job1 + Dispatchers.IO + CoroutineName("te666st"))
        val job2 = scope.launch {
            val job3 = coroutineContext[Job]
            println("job3打印 $job3")//job3打印 "te666st#2":StandaloneCoroutine{Active}@6a5fc7f7
        }
        println("job2打印 $job2")//job2打印 "te666st#2":StandaloneCoroutine{Active}@6a5fc7f7
        job2.join()
    }


    @Test
    fun `test CoroutineContext extend2`() = runBlocking<Unit> {
        val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception -> //用不到的参数用下划线代替，赋值的时候会跳过下划线的参数，这样可以节省内存
            println("Caught $exception")
        }
        val scope = CoroutineScope(
            Job() + Dispatchers.Main + coroutineExceptionHandler
        )

        val job = scope.launch(Dispatchers.IO) {
            //新协程
        }
    }

    @Test
    fun `test exception propagation 0`() = runBlocking<Unit> {
        val job = GlobalScope.launch {
            try {
                throw IndexOutOfBoundsException()
            } catch (e: Exception) {
                println("Caught IndexOutOfBoundsException")
            }
        }
        job.join()

        val deferred = GlobalScope.async {
            println("async")
            throw ArithmeticException()
        }
        deferred.await()
    }

    @Test
    fun `test exception propagation`() = runBlocking<Unit> {
        val job = GlobalScope.launch {
            try {
                throw IndexOutOfBoundsException()
            } catch (e: Exception) {
                println("Caught IndexOutOfBoundsException")
            }
        }
        job.join()

        val deferred = GlobalScope.async {
            println("async")
            throw ArithmeticException()
        }

        /*try {
            deferred.await()
        }catch (e:Exception){
            println("Caught ArithmeticException")
        }*/

        delay(1000)
    }


    @Test
    fun `test exception propagation2`() = runBlocking<Unit> {
        val scope = CoroutineScope(Job())
        val job = scope.launch {
            async {
                throw IllegalArgumentException()
            }
        }
        job.join()
    }

    @Test
    fun `test SupervisorJob`() = runBlocking<Unit> {
        val supervisor = CoroutineScope(SupervisorJob())
        val job1 = supervisor.launch {
            delay(100)
            println("child 1")
            throw IllegalArgumentException()
        }

        val job2 = supervisor.launch {
            try {
                delay(Long.MAX_VALUE)
            } finally {
                println("child 2 finished.")
            }
        }

        delay(200)
        supervisor.cancel()
        joinAll(job1, job2)
    }

    @Test
    fun `test supervisorScope`() = runBlocking<Unit> {
        supervisorScope {
            launch {
                delay(100)
                println("child 1")
                throw IllegalArgumentException()
            }

            try {
                delay(Long.MAX_VALUE)
            } finally {
                println("child 2 finished.")
            }
        }
    }

    @Test
    fun `test supervisorScope2`() = runBlocking<Unit> {
        supervisorScope {
            val child = launch {
                try {
                    println("The child is sleeping")
                    delay(Long.MAX_VALUE)
                } finally {
                    println("The child is cancelled")
                }
            }
            yield()
            println("Throwing an exception from the scope")
            throw AssertionError()
        }
    }

    @Test
    fun `test CoroutineExceptionHandler`() = runBlocking<Unit> {
        val handler = CoroutineExceptionHandler { _, exception ->
            println("Caught $exception")
        }

        val job = GlobalScope.launch(handler) {
            throw AssertionError()
        }

        val deferred = GlobalScope.async(handler) {
            throw ArithmeticException()
        }

        job.join()
        deferred.await()
    }

    @Test
    fun `test CoroutineExceptionHandler2`() = runBlocking<Unit> {
        val handler = CoroutineExceptionHandler { _, exception ->
            println("Caught $exception")
        }
        val scope = CoroutineScope(Job())
        val job = scope.launch(handler) {
            launch {
                throw IllegalArgumentException()
            }
        }
        job.join()
    }

    @Test
    fun `test CoroutineExceptionHandler3`() = runBlocking<Unit> {
        val handler = CoroutineExceptionHandler { _, exception ->
            println("Caught $exception")
        }
        val scope = CoroutineScope(Job())
        val job = scope.launch {
            launch(handler) {
                throw IllegalArgumentException()
            }
        }
        job.join()
    }


    @Test
    fun `test cancel and exception`() = runBlocking<Unit> {
        val job = launch {
            val child = launch {
                try {
                    delay(Long.MAX_VALUE)
                } finally {
                    println("Child is cancelled.")
                }
            }
            yield()
            println("Cancelling child")
            child.cancelAndJoin()
            yield()
            println("Parent is not cancelled")
        }
        job.join()
    }


    @Test
    fun `test cancel and exception2`() = runBlocking<Unit> {
        val handler = CoroutineExceptionHandler { _, exception ->
            println("Caught $exception")
        }

        val job = GlobalScope.launch(handler) {
            launch {
                try {
                    delay(Long.MAX_VALUE)
                } finally {
                    withContext(NonCancellable) {
                        println("Children are cancelled, but exception is not handled until all children terminate")
                        delay(100)
                        println("The first child finished its non cancellable block")
                    }
                }
            }

            launch {
                delay(10)
                println("Second child throws an exception")
                throw ArithmeticException()
            }
        }
        job.join()
    }


    @Test
    fun `test exception aggregation`() = runBlocking<Unit> {
        val handler = CoroutineExceptionHandler { _, exception ->
            println("Caught $exception  ${exception.suppressed.contentToString()}")
        }

        val job = GlobalScope.launch(handler) {
            launch {
                try {
                    delay(Long.MAX_VALUE)
                } finally {
                    throw ArithmeticException()  //2
                }
            }

            launch {
                try {
                    delay(Long.MAX_VALUE)
                } finally {
                    throw IndexOutOfBoundsException()  //3
                }
            }

            launch {
                delay(100)
                throw IOException()  //1
            }
        }

        job.join()
    }

}


