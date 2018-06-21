package com.jfz.app;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.jfz.library.lib1.HelloLib1;
import com.jfz.library.lib2.HelloLib2;

public class SecondActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        HelloLib1.show();

        HelloLib2.show();

    }
}
