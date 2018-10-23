package com.jfz.lint;

import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Context;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;

import org.jetbrains.annotations.NotNull;

public class MyCheckDetector extends Detector implements Detector.ClassScanner {

    private static final Implementation IMPLEMENTATION =
            new Implementation(MyCheckDetector.class, Scope.MANIFEST_SCOPE);

    /**
     * Wrong order of elements in the manifest
     */
    public static final Issue ORDER =
            Issue.create(
                    "ManifestOrder",
                    "Incorrect order of elements in manifest",
                    "The <application> tag should appear after the elements which declare "
                            + "which version you need, which features you need, which libraries you "
                            + "need, and so on. In the past there have been subtle bugs (such as "
                            + "themes not getting applied correctly) when the `<application>` tag appears "
                            + "before some of these other elements, so it's best to order your "
                            + "manifest in the logical dependency order.",
                    Category.CORRECTNESS,
                    5,
                    Severity.WARNING,
                    IMPLEMENTATION);

    static final Issue Test = Issue.create("test",
            "test desc",
            "this explanation",
            Category.CORRECTNESS,
            4, Severity.WARNING,
            IMPLEMENTATION);

    @Override
    public void beforeCheckRootProject(@NotNull Context context) {
        super.beforeCheckRootProject(context);
        System.out.println("MyCheckDetector.beforeCheckRootProject");
    }

    @Override
    public void beforeCheckFile(@NotNull Context context) {
        super.beforeCheckFile(context);
        System.out.println("MyCheckDetector.beforeCheckFile");
    }

    @Override
    public void afterCheckFile(@NotNull Context context) {
        super.afterCheckFile(context);

        System.out.println("MyCheckDetector.afterCheckFile");
    }
}
