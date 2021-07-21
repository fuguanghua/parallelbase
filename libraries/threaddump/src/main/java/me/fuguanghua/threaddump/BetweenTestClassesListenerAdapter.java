package me.fuguanghua.threaddump;

import org.testng.IClassListener;
import org.testng.ITestClass;
import org.testng.ITestContext;
import org.testng.ITestListener;

/**
 * TestNG listener adapter for detecting when execution finishes in previous
 * test class and starts in a new class.
 */
abstract class BetweenTestClassesListenerAdapter implements IClassListener, ITestListener {
    Class<?> lastTestClass;

    @Override
    public void onBeforeClass(ITestClass testClass) {
        checkIfTestClassChanged(testClass.getRealClass());
    }

    private void checkIfTestClassChanged(Class<?> testClazz) {
        if (lastTestClass != testClazz) {
            onBetweenTestClasses(lastTestClass, testClazz);
            lastTestClass = testClazz;
        }
    }

    @Override
    public void onFinish(ITestContext context) {
        if (lastTestClass != null) {
            onBetweenTestClasses(lastTestClass, null);
            lastTestClass = null;
        }
    }

    /**
     * Call back hook for adding logic when test execution moves from test class to another.
     *
     * @param endedTestClass the test class which has finished execution. null if the started test class is the first
     * @param startedTestClass the test class which has started execution. null if the ended test class is the last
     */
    protected abstract void onBetweenTestClasses(Class<?> endedTestClass, Class<?> startedTestClass);
}
