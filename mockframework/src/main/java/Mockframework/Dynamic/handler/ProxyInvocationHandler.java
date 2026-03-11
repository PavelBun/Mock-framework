package Mockframework.Dynamic.handler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public final class ProxyInvocationHandler implements InvocationHandler {
    private final DynamicMockHandler mockHandler = new DynamicMockHandler();

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return mockHandler.handle(proxy, method, args);
    }
}