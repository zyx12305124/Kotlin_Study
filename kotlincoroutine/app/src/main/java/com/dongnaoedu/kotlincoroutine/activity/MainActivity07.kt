package com.dongnaoedu.kotlincoroutine.activity

import android.annotation.SuppressLint
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.dongnaoedu.kotlincoroutine.R
import com.dongnaoedu.kotlincoroutine.api.User
import com.dongnaoedu.kotlincoroutine.api.userServiceApi
import com.dongnaoedu.kotlincoroutine.databinding.ActivityMainBinding
import com.dongnaoedu.kotlincoroutine.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 *
 * @author ningchuanqi
 * @version V1.0
 */
class MainActivity07 : AppCompatActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    @SuppressLint("StaticFieldLeak","SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this,R.layout.activity_main)
        binding.viewModel = mainViewModel
        binding.lifecycleOwner = this
        binding.submitButton.setOnClickListener {
            mainViewModel.getUser("xxx")
        }

        val kuKe = KuKe()
        val leiJun = LeiJun(kuKe)

        leiJun.homeWork
        leiJun.write() //KuKe(): 写作业写作业...
        leiJun.playGame() //LeiJun(): 玩游戏，爽...
    }

}



//类委托


interface HomeWork {
    fun write()
}

class KuKe() : HomeWork {
    override fun write() {

        Log.i("biubiu", "KuKe(): 写作业写作业...")
    }
}


class LeiJun(val homeWork: HomeWork) : HomeWork by homeWork {
    fun playGame() {

        Log.i("biubiu", "LeiJun(): 玩游戏，爽...")
    }

}
