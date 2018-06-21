package com.jfz.app.app1

import android.app.Activity
import android.os.Bundle
import android.widget.Toast

/**
 * Created by act.zhang on 2018/6/20.
 *
 * @author zhangchaoxian@jinfuzi.com
 */
class App1Activity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        print("hello")

        Toast.makeText(this, "App1Activity", Toast.LENGTH_SHORT).show()
    }
}