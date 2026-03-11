package Mockframework.Dynamic.handler;

import Mockframework.Core.Answer;
import Mockframework.Dynamic.registry.DynamicStubbingRegistry;
import Mockframework.Dynamic.registry.InvocationKey;

import java.lang.reflect.Method;

public final class DynamicMockHandler {
    private final DynamicStubbingRegistry registry = DynamicStubbingRegistry.getInstance();

    public Object handle(Object mock, Method method, Object[] args) throws Throwable {
        // Пропускаем методы Object (toString, hashCode, equals)
        if (isObjectMethod(method)) {
            return handleObjectMethod(mock, method, args);
        }

        InvocationKey key = new InvocationKey(mock, method, args);
        // Сохраняем последний вызов для when()
        registry.recordLastInvocation(key);

        // Ищем заглушку
        Answer answer = registry.getStub(key);
        if (answer != null) {
            return answer.answer(args);
        }
        return defaultValue(method.getReturnType());
    }

    private boolean isObjectMethod(Method method) {
        return method.getDeclaringClass() == Object.class;
    }

    private Object handleObjectMethod(Object mock, Method method, Object[] args) {
        String name = method.getName();
        if ("toString".equals(name)) {
            return "Mock for " + mock.getClass().getSimpleName() + "@" + Integer.toHexString(System.identityHashCode(mock));
        } else if ("hashCode".equals(name)) {
            return System.identityHashCode(mock);
        } else if ("equals".equals(name)) {
            return mock == args[0];
        }
        throw new RuntimeException("Unexpected Object method: " + method);
    }

    private Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) return null;
        if (type == boolean.class) return false;
        if (type == char.class) return '\0';
        if (type == byte.class) return (byte) 0;
        if (type == short.class) return (short) 0;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == float.class) return 0.0f;
        if (type == double.class) return 0.0d;
        // void не должен сюда попадать, но на всякий случай
        return null;
    }
}