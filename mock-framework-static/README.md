# mock-framework-static

Модуль реализует статические моки через Java Agent + Byte Buddy.

## Публичный API

```java
import ru.pozhidaev.mockframework.MockedStatic;
import ru.pozhidaev.mockframework.Mockito;

try (MockedStatic<MyUtils> mocked = Mockito.mockStatic(MyUtils.class)) {
    mocked.when(() -> MyUtils.sum(1, 2)).thenReturn(42);
    mocked.when(MyUtils::ping).thenThrow(new IllegalStateException("boom"));
}
```

API классы:
- `Mockito.mockStatic(Class<T>)`
- `MockedStatic<T>#when(StaticInvocation<R>)`
- `OngoingStubbing<R>#thenReturn(...)`
- `OngoingStubbing<R>#thenThrow(...)`
- `OngoingStubbing<R>#thenAnswer(...)`

## Как это работает

1. На старте JVM метод `MockAgent.premain(...)` регистрирует `AgentBuilder` (Byte Buddy).
2. Агент трансформирует только статические `non-native` методы целевых классов.
3. Тело метода подменяется через `MethodDelegation` на `StaticMethodStub.intercept(...)`.
4. `StaticMethodStub`:
   - вычисляет `MethodSignature` в формате `methodName(paramType1,paramType2)`;
   - ищет стаб в `StaticMockManager`;
   - если стаб найден, вызывает `Answer`;
   - если нет, вызывает оригинальный метод через `@SuperCall`.
5. `MockedStaticImpl.when(...)` создает стаббинг:
   - сначала пытается извлечь сигнатуру из method reference (`SerializedLambda`);
   - если не удалось, использует режим захвата вызова через `ThreadLocal`.
6. При `close()` удаляются только стаббинги, созданные этим `MockedStatic`.

## Реестр стаббингов

`StaticMockManager` хранит состояние в `ThreadLocal<Map<MethodKey, Answer>>`.

Ключ:
- `Class<?>`
- `MethodSignature`

Это дает:
- изоляцию между потоками/тестами;
- корректную работу перегруженных методов.

## Ограничения текущей реализации

- Трансформация ограничена классами проекта (`ru.pozhidaev.mockframework.*`) для безопасности рантайма тестов.
- `native`-статические методы не перехватываются.
- JDK-статические методы (например, `System.currentTimeMillis`) в текущей конфигурации не поддерживаются.
- В одном `when(...)` ожидается один целевой статический вызов.

## Запуск тестов

```bash
gradle -q :mock-framework-static:test
```

Таск `test` автоматически добавляет `-javaagent` на JAR модуля.

Для отладки загрузки классов:

```bash
gradle -q :mock-framework-static:test -Dmock.framework.debug=true
```

