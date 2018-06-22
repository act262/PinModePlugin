package com.jfz.library;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Toast;

/**
 * Created by act.zhang on 2018/6/21.
 *
 * @author zhangchaoxian@jinfuzi.com
 */
public class LibActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        System.out.println("LibActivity.onCreate");
        Toast.makeText(this, "LibActivity", Toast.LENGTH_SHORT).show();
    }
}
