package Mockframework.Static;

import java.util.Objects;

import Mockframework.Core.matcher.ArgumentMatchers;
import Mockframework.Static.staticmock.MockedStaticImpl;
import Mockframework.Static.staticmock.StaticMockManager;

public final class Mockito {
    private Mockito() {
    }

    public static <T> MockedStatic<T> mockStatic(Class<T> clazz) {
        return new MockedStaticImpl<>(Objects.requireNonNull(clazz, "clazz must not be null"));
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
}
