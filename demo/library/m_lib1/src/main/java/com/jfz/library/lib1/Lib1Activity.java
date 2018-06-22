package com.jfz.library.lib1;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Toast;

/**
 * Created by act.zhang on 2018/6/21.
 *
 * @author zhangchaoxian@jinfuzi.com
 */
public class Lib1Activity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("Lib1Activity.onCreate");

        Toast.makeText(this, "Lib1Activity", Toast.LENGTH_SHORT).show();
    }
}
