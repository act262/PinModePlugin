package com.jfz.app.app2;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Toast;

/**
 * Created by act.zhang on 2018/6/21.
 *
 * @author zhangchaoxian@jinfuzi.com
 */
public class App2Activity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Toast.makeText(this, "App2Activity", Toast.LENGTH_SHORT).show();
    }
}
