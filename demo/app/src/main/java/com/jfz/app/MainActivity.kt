package com.jfz.app

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.jfz.app.app1.App1Activity
import com.jfz.app.app2.App2Activity
import com.jfz.library.LibActivity
import com.jfz.library.lib1.Lib1Activity
import com.jfz.library.lib2.Lib2Activity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun toSecondActivity(view: View) {
        startActivity(Intent(this, SecondActivity::class.java))
    }

    fun toApp1Activity(view: View) {
        startActivity(Intent(this, App1Activity::class.java))
    }

    fun toApp2Activity(view: View) {
        startActivity(Intent(this, App2Activity::class.java))
    }

    fun toLibActivity(view: View) {
        startActivity(Intent(this, LibActivity::class.java))
    }

    fun toLib1Activity(view: View) {
        startActivity(Intent(this, Lib1Activity::class.java))
    }

    fun toLib2Activity(view: View) {
        startActivity(Intent(this, Lib2Activity::class.java))
    }
}
