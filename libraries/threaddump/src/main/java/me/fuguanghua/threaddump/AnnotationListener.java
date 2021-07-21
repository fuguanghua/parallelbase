package me.fuguanghua.threaddump;

import org.testng.IAnnotationTransformer;
import org.testng.annotations.IConfigurationAnnotation;
import org.testng.annotations.ITestAnnotation;
import org.testng.annotations.ITestOrConfiguration;
import org.testng.internal.annotations.DisabledRetryAnalyzer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

public class AnnotationListener implements IAnnotationTransformer {

    private static final long DEFAULT_TEST_TIMEOUT_MILLIS = TimeUnit.MINUTES.toMillis(5);
    private static final String OTHER_GROUP = "other";

    public AnnotationListener() {
        System.out.println("Created annotation listener");
    }

    @Override
    public void transform(ITestAnnotation annotation,
                          Class testClass,
                          Constructor testConstructor,
                          Method testMethod) {
        if (annotation.getRetryAnalyzerClass() == null
                || annotation.getRetryAnalyzerClass() == DisabledRetryAnalyzer.class) {
            annotation.setRetryAnalyzer(RetryAnalyzer.class);
        }

        // Enforce default test timeout
        if (annotation.getTimeOut() == 0) {
            annotation.setTimeOut(DEFAULT_TEST_TIMEOUT_MILLIS);
        }

        addToOtherGroupIfNoGroupsSpecified(annotation);
    }

    private void addToOtherGroupIfNoGroupsSpecified(ITestOrConfiguration annotation) {
        // Add test to "other" group if there's no specified group
        if (annotation.getGroups() == null || annotation.getGroups().length == 0) {
            annotation.setGroups(new String[]{OTHER_GROUP});
        }
    }

    @Override
    public void transform(IConfigurationAnnotation annotation, Class testClass, Constructor testConstructor,
                          Method testMethod) {
        // configuration methods such as BeforeMethod / BeforeClass methods should also be added to the "other" group
        // since BeforeMethod/BeforeClass methods get run only when the group matches or when there's "alwaysRun=true"
        addToOtherGroupIfNoGroupsSpecified(annotation);
    }
}
