package com.jfz.library.lib2

import android.app.Activity
import android.os.Bundle
import android.widget.Toast

/**
 * Created by act.zhang on 2018/6/21.
 *
 * @author zhangchaoxian@jinfuzi.com
 */

class Lib2Activity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        System.out.println("Lib2Activity.onCreate")

        Toast.makeText(this, "Lib2Activity", Toast.LENGTH_SHORT).show()
    }
}