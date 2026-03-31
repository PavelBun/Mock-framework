package Mockframework.examples;

public class DemoStaticApiService {

    public String processUser(String name) {
        // 1. Берём текущее время
        long timestamp = DemoStaticApi.now();

        // 2. Делаем приветствие
        String greeting = DemoStaticApi.greet(name);

        // 3. Склеиваем данные
        String result = DemoStaticApi.join(greeting, String.valueOf(timestamp));

        // 4. Пишем аудит
        DemoStaticApi.audit("Processed user: " + name);

        return result;
    }

    public String safeProcess(String input) {
        try {
            // потенциально "нестабильный" вызов
            String value = DemoStaticApi.unstable(input);

            DemoStaticApi.audit("Success: " + input);
            return value;

        } catch (Exception e) {
            // fallback логика
            DemoStaticApi.audit("Error: " + input);
            return "fallback-" + input;
        }
    }

    public boolean isFreshCall(long thresholdMillis) {
        long now = DemoStaticApi.now();
        return now % thresholdMillis == 0;
    }
}