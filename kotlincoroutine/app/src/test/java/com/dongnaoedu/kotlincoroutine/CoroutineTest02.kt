package com.dongnaoedu.kotlincoroutine

import kotlinx.coroutines.*
import org.junit.Test

/**
 *
 * @author ningchuanqi
 * @version V1.0
 */
class CoroutineTest02 {

    //作用域构建器
    //runBlocking是常规函数，而coroutineScope是挂起函数。
    //它们看起来很类似，它们都会等待其协程体以及所有子协程结束。
    //两者的区别 runBlocking是常规函数 coroutineScope是挂起函数
    //主要区别在于runBlocking方法会阻塞当前线程来等待，而coroutineScope只是挂起，会释放底层线程用于其他用途。
    @Test
    fun `test coroutine scope builder`() = runBlocking<Unit> {
        //一个协程失败了，所有其他兄弟协程也会被取消
        coroutineScope {//coroutineScope就是一个协程的作用域的构建器

            //启动两个子协程
            val job1 = launch {
                delay(400)
                println("job1 finished.")
            }
            val job2 = launch {
                delay(200)
                println("job2 finished.")
//                throw IllegalArgumentException()//coroutineScope 只要有一个协程失败了，所有其他的兄弟协程也会被取消

            }
            //coroutineScope 一定要等job1 job2这两个子协程执行完毕 这一整个作用域才算执行结束
            //coroutineScope 和 runBlocking{} 很像 都是要等子协程执行完毕


        }
    }

    //supervisorScope
    //一个协程失败了，不会影响其他兄弟协程
    @Test
    fun `test supervisor scope builder`() = runBlocking<Unit> {
        supervisorScope {
            val job1 = launch {
                delay(400)
                println("job1 finished.") //job2出问题 job1不会被影响到 这句还是会被打印
            }
            val job2 = launch {
                delay(200)
                println("job2 finished.")
                throw IllegalArgumentException()
            }
        }
    }

    //对于每一个创建的协程（通过launch或者async），会返回一个Job实例，该实例是协程的唯一标示，并且负责管理协程的生命周期。
    //Job的生命周期
    //一个任务可以包含一系列状态：
    //新创建（New）、活跃（Active）、完成中（Completing）、已完成（Completed）、取消中（Cancelling）和已取消（Cancelled）。
    //虽然我们无法直接访问这些状态，但是我们可以访问Job的属性：isActive、isCancelled和isCompleted。

    //如果协程处于活跃状态，协程运行出错或者调用 job.cancel() 都会将当前任务置为取消中 (Cancelling) 状态 (isActive = false, isCancelled = true)。
    //当所有的子协程都完成后，协程会进入已取消 (Cancelled) 状态，此时 isCompleted = true。

}