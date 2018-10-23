package com.jfz.lint;

import com.android.tools.lint.client.api.IssueRegistry;
import com.android.tools.lint.detector.api.Issue;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * Created by act.zhang on 2018/10/10.
 *
 * @author zhangchaoxian@jinfuzi.com
 */
public class Registry extends IssueRegistry {

    @NotNull
    @Override
    public List<Issue> getIssues() {
        return Arrays.asList(MyCheckDetector.Test, MyCheckDetector.ORDER);
    }
}
