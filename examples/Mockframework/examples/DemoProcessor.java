package Mockframework.examples;

public final class DemoProcessor {
    private final BillingClient billingClient;

    public DemoProcessor(BillingClient billingClient) {
        this.billingClient = billingClient;
    }

    public String process(String rawUser, int amount) {
        String normalized = billingClient.normalize(rawUser);
        int charged = billingClient.charge(normalized, amount);
        String greeting = DemoStaticApi.greet(normalized);
        DemoStaticApi.audit("charged:" + charged);
        return greeting + ":" + charged;
    }
}
