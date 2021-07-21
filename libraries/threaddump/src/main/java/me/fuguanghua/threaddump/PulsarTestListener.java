package me.fuguanghua.threaddump;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.internal.thread.ThreadTimeoutException;

import java.util.Arrays;

public class PulsarTestListener implements ITestListener {

    @Override
    public void onTestStart(ITestResult result) {
        System.out.format("------- Starting test %s.%s(%s)-------\n", result.getTestClass(),
                result.getMethod().getMethodName(), Arrays.toString(result.getParameters()));
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        System.out.format("------- SUCCESS -- %s.%s(%s)-------\n", result.getTestClass(),
                result.getMethod().getMethodName(), Arrays.toString(result.getParameters()));
    }

    @Override
    public void onTestFailure(ITestResult result) {
        if (!(result.getThrowable() instanceof SkipException)) {
            System.out.format("!!!!!!!!! FAILURE-- %s.%s(%s)-------\n", result.getTestClass(),
                    result.getMethod().getMethodName(), Arrays.toString(result.getParameters()));
        }
        if (result.getThrowable() instanceof ThreadTimeoutException) {
            System.out.println("====== THREAD DUMPS ======");
            System.out.println(ThreadDumpUtil.buildThreadDiagnosticString());
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        System.out.format("~~~~~~~~~ SKIPPED -- %s.%s(%s)-------\n", result.getTestClass(),
                result.getMethod().getMethodName(), Arrays.toString(result.getParameters()));
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {

    }

    @Override
    public void onStart(ITestContext context) {

    }

    @Override
    public void onFinish(ITestContext context) {
    }
}
