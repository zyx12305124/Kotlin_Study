package learn.gouzi.bilibilijetpack.kotlin.by

import android.util.Log

//创建接口
interface Base {

    fun print()
}

//实现此接口的被委托的类
class BaseImp(val x:Int) : Base {
    override fun print() {

        println(x)
    }
}

//通过关键字by建立委托类
class Derived (b:Base):Base by b


class Main {

    companion object{

        @JvmStatic
        fun main(args: Array<String>) {
            var baseImp=BaseImp(100)
            Derived(baseImp).print()  //输出100

        }
    }




}


//_________________________________________________________________________________________________________________

