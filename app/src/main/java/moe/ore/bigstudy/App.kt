package moe.ore.bigstudy

import android.app.Application
import android.content.SharedPreferences
import com.haoge.easyandroid.EasyAndroid
import com.xuexiang.xui.XUI

lateinit var config: SharedPreferences

class App: Application() {
    override fun onCreate() {
        super.onCreate()
        XUI.init(this)
        EasyAndroid.init(this)

        config = getSharedPreferences("dxx_config", MODE_PRIVATE)
    }
}