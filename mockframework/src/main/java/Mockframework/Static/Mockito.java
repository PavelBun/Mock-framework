package Mockframework.Static;

import java.util.Objects;

import Mockframework.Core.matcher.ArgumentMatchers;
import Mockframework.Static.staticmock.MockedStaticImpl;
import Mockframework.Static.staticmock.StaticMockManager;
import Mockframework.Static.verification.AtLeast;
import Mockframework.Static.verification.AtMost;
import Mockframework.Static.verification.Times;
import Mockframework.Static.verification.VerificationMode;

public final class Mockito {
    private Mockito() {
    }

    public static <T> MockedStatic<T> mockStatic(Class<T> clazz) {
        Class<T> targetClass = Objects.requireNonNull(clazz, "clazz must not be null");
        String className = targetClass.getName();
        if (className.startsWith("java.") || className.startsWith("jdk.") || className.startsWith("sun.")) {
            throw new UnsupportedOperationException(
                "Mocking core Java classes is not supported: " + className
            );
        }
        return new MockedStaticImpl<>(targetClass);
    }

    public static <T> T any() {
        StaticMockManager.registerMatcher(ArgumentMatchers.any());
        return null;
    }

    public static <T> T eq(T value) {
        StaticMockManager.registerMatcher(ArgumentMatchers.eq(value));
        return value;
    }

    public static String contains(String value) {
        StaticMockManager.registerMatcher(ArgumentMatchers.contains(value));
        return value;
    }

    public static VerificationMode times(int count) {
        return new Times(count);
    }

    public static VerificationMode never() {
        return new Times(0);
    }

    public static VerificationMode atLeast(int min) {
        return new AtLeast(min);
    }

    public static VerificationMode atMost(int max) {
        return new AtMost(max);
    }
}
