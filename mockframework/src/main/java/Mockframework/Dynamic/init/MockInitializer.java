package Mockframework.Dynamic.init;

import Mockframework.Dynamic.annotation.Mock;
import Mockframework.Dynamic.creator.DynamicMockCreator;

import java.lang.reflect.Field;

public final class MockInitializer {

    private MockInitializer() {}

    public static void initMocks(Object testInstance) {
        Field[] fields = testInstance.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Mock.class)) {
                Class<?> type = field.getType();
                Object mock = DynamicMockCreator.createMock(type);
                field.setAccessible(true);
                try {
                    field.set(testInstance, mock);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to inject mock into field: " + field.getName(), e);
                }
            }
        }
    }
}