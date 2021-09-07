package com.dongnaoedu.kotlincoroutineexception

import kotlinx.coroutines.*
import org.junit.Test
import java.io.IOException
import kotlin.AssertionError

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
        //CoroutineExceptionHandler协程的异常处理器
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
            throw IndexOutOfBoundsException()
            //用launch构建，异常会在它发生的第一时间被抛出  同理actor
        }
        job.join()

        val deferred = GlobalScope.async {
            println("async")
            throw ArithmeticException()
            //用async构建协程，则依赖用户来最终消费异常 同理produce
        }
        deferred.await()
    }

    @Test
    fun `test exception propagation 1`() = runBlocking<Unit> {
        val job = GlobalScope.launch {
            try{
                throw IndexOutOfBoundsException()
            } catch (e:Exception){
                println("Caught IndexOutOfBoundsException")
            }
        }
        job.join()

//        val deferred = GlobalScope.async {
//            println("async")
//            throw ArithmeticException()
//        }
//        deferred.await()
    }

    //根协程 异常的传递
    @Test
    fun `test exception propagation`() = runBlocking<Unit> {
        val job = GlobalScope.launch {
//            try {
                throw IndexOutOfBoundsException()
//            } catch (e: Exception) {
//                println("Caught IndexOutOfBoundsException")
//            }
        }
//        try {
            job.join()
//        } catch (e: Exception) {
//            println("Caught IndexOutOfBoundsException 222")
//        }

        val deferred = GlobalScope.async {
                println("async")
                throw ArithmeticException()
        }
        /**
        try {
            deferred.await() //用async启动的协程 在调用await的时候可以捕获到异常
        }catch (e:Exception){
            println("Caught ArithmeticException 222")
        }
         这句注掉不会抛出异常 因为只有用户调用的时候才会抛出异常
         */

        delay(1000)
    }

    //非根协程（协程的子协程） 异常的传递
    //非根协程所创建的协程中，产生的异常总是会被传播
    @Test
    fun `test exception propagation2`() = runBlocking<Unit> {
        val scope = CoroutineScope(Job())
        val job = scope.launch {
            async {
                throw IllegalArgumentException()//非根协程的async直接就可以引发异常，不需要调用await()
            }
        }
        job.join()
    }

    /**
     * 异常的传播特性（传播的过程）
     * 当一个协程由于一个异常运行失败时，它会传播这个异常并传递给它的父级，接下来，父级会进行一下几步操作：
     * 1.取消它自己的子级
     * 2.取消它自己
     * 3.讲异常传播并传递给它的父级
     */


    /**
     * 像上面这种 一个协程失败，其他都取消了也不太好，如何打破异常的传播特性呢？
     * 使用SupervisorJob时，一个子协程失败不会影响到其他子协程。
     * supervisorJob不会传播异常给它的父级，它会让子协程自己处理异常
     */
    @Test
    fun `test SupervisorJob 0`() = runBlocking<Unit> {
        val supervisor = CoroutineScope(SupervisorJob())
//        val supervisor = CoroutineScope(Job())
        val job1 = supervisor.launch {
            delay(100)
            println("child 1")
            throw IllegalArgumentException()
        }

        val job2 = supervisor.launch {
            try {
                delay(Long.MAX_VALUE)
//                delay(10000)
            } finally {
                println("child 2 finished.")
                //把协程里的参数换成Job() child1 child2都会打印 则表示 child1的异常影响到了child2的执行
                //如果是supervisorJob()则child2不会打印 则表示child2不受到child1的影响
                //使用supervisorJob（）一个协程失败，不影响其他的兄弟协程
            }
        }

         joinAll(job1, job2)
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
        supervisor.cancel()//这里如果整个作用域被取消 则job1job2都被取消
        joinAll(job1, job2)
    }

    //使用supervisorScope 也可以达到和supervisorJob同样的效果
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
//                delay(10000)
            } finally {
                println("child 2 finished.")
            }
            //打印结果只打印child1 ，child2不打印 表示child2上面的delay没有受到child1的影响
        }
        //同样的如果作用域自身失败的时候，所有的子作业都会被取消
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
            yield()//出让线程执行权
            println("Throwing an exception from the scope")
            throw AssertionError()
            //打印结果
            /**
            The child is sleeping
            Throwing an exception from the scope
            The child is cancelled
             */
        }
    }

    /**
     * 使用CoroutineExceptionHandler来对协程的异常进行捕获。
     * 但不是所有的协程异常都会被捕获到，满足以下条件时，异常就会被捕获：
     * 1.时机：异常是自动抛出异常的协程所抛出的，使用launch而不是async时
     * 2.位置：在CoroutineScope的CoroutineContext中或在一个根协程（CoroutineScope或者SuperVisorScope的直接子协程）中。
     */
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


