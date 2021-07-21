package me.fuguanghua.threaddump;

import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.util.RetryAnalyzerCount;

public class RetryAnalyzer extends RetryAnalyzerCount {
    // Only try again once
    static final int MAX_RETRIES = Integer.parseInt(System.getProperty("testRetryCount", "1"));

    public RetryAnalyzer() {
        setCount(MAX_RETRIES);
    }

    @Override
    public boolean retryMethod(ITestResult result) {
        return !(result.getThrowable() instanceof SkipException);
    }
}
