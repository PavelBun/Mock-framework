package Mockframework.Dynamic.creator;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import Mockframework.Dynamic.handler.ByteBuddyInterceptor;
import Mockframework.Dynamic.handler.ProxyInvocationHandler;

import java.lang.reflect.Proxy;

public final class DynamicMockCreator {

    private DynamicMockCreator() {}

    @SuppressWarnings("unchecked")
    public static <T> T createMock(Class<T> typeToMock) {
        if (typeToMock.isInterface()) {
            return createInterfaceMock(typeToMock);
        } else {
            return createClassMock(typeToMock);
        }
    }

    private static <T> T createInterfaceMock(Class<T> interfaceType) {
        ProxyInvocationHandler handler = new ProxyInvocationHandler();
        return (T) Proxy.newProxyInstance(
                interfaceType.getClassLoader(),
                new Class<?>[]{interfaceType},
                handler);
    }

    private static <T> T createClassMock(Class<T> classToMock) {
        try {
            return new ByteBuddy()
                    .subclass(classToMock)
                    .method(ElementMatchers.any())
                    .intercept(MethodDelegation.to(new ByteBuddyInterceptor()))
                    .make()
                    .load(classToMock.getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
                    .getLoaded()
                    .getDeclaredConstructor()
                    .newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create mock for class: " + classToMock +
                    ". Ensure it has a default constructor.", e);
        }
    }
}