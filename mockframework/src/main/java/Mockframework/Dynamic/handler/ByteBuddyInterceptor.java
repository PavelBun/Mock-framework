package Mockframework.Dynamic.handler;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;

import java.lang.reflect.Method;

public final class ByteBuddyInterceptor {
    private final DynamicMockHandler mockHandler = new DynamicMockHandler();

    @RuntimeType
    public Object intercept(@This Object mock, @Origin Method method, @AllArguments Object[] args) throws Throwable {
        return mockHandler.handle(mock, method, args);
    }
}