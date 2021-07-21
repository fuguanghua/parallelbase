package me.fuguanghua.threaddump;

import org.testng.annotations.DataProvider;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * TestNG DataProvider for passing all Enum values as parameters to a test method.
 *
 * Supports currently a single Enum parameter for a test method.
 */
public abstract class EnumValuesDataProvider {
    @DataProvider
    public static final Object[][] values(Method testMethod) {
        Class<?> enumClass = Arrays.stream(testMethod.getParameterTypes())
                .findFirst()
                .filter(Class::isEnum)
                .orElseThrow(() -> new IllegalArgumentException("The test method should have an enum parameter."));
        return toDataProviderArray((Class<? extends Enum<?>>) enumClass);
    }

    /*
     * Converts all values of an Enum class to a TestNG DataProvider object array
     */
    public static Object[][] toDataProviderArray(Class<? extends Enum<?>> enumClass) {
        Enum<?>[] enumValues = enumClass.getEnumConstants();
        return Stream.of(enumValues)
                .map(enumValue -> new Object[]{enumValue})
                .collect(Collectors.toList())
                .toArray(new Object[0][]);
    }
}
