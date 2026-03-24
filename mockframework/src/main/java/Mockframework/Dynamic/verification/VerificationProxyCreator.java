package Mockframework.Dynamic.verification;

import Mockframework.Dynamic.handler.VerificationInvocationHandler;
import Mockframework.Dynamic.handler.VerificationInterceptor;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Proxy;

public final class VerificationProxyCreator {

    private VerificationProxyCreator() {}

    @SuppressWarnings("unchecked")
    public static <T> T createVerificationProxy(T mock, VerificationMode mode) {
        if (mock == null) {
            throw new IllegalArgumentException("mock cannot be null");
        }
        Class<?> mockClass = mock.getClass();
        // Если mock является JDK-прокси или интерфейсом, используем Proxy
        if (mockClass.isInterface() || Proxy.isProxyClass(mockClass)) {
            Class<?>[] interfaces = mockClass.getInterfaces();
            if (interfaces.length == 0) {
                throw new IllegalArgumentException("Cannot verify mock: no interfaces found");
            }
            return (T) Proxy.newProxyInstance(
                    interfaces[0].getClassLoader(),
                    new Class<?>[]{interfaces[0]},
                    new VerificationInvocationHandler(mock, mode)
            );
        } else {
            // Для обычных классов используем Byte Buddy
            return createClassProxy(mock, mode);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T createClassProxy(T mock, VerificationMode mode) {
        Class<?> type = mock.getClass();
        // Защита: если всё же попал прокси, используем JDK Proxy
        if (type.isInterface() || Proxy.isProxyClass(type)) {
            return (T) Proxy.newProxyInstance(
                    type.getClassLoader(),
                    new Class<?>[]{type},
                    new VerificationInvocationHandler(mock, mode)
            );
        }
        try {
            return (T) new ByteBuddy()
                    .subclass(type)
                    .method(ElementMatchers.any())
                    .intercept(MethodDelegation.to(new VerificationInterceptor(mock, mode)))
                    .make()
                    .load(type.getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
                    .getLoaded()
                    .getDeclaredConstructor()
                    .newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create verification proxy for class: " + type, e);
        }
    }
}