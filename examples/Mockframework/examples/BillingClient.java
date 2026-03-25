package Mockframework.examples;

public interface BillingClient {
    String normalize(String value);

    int charge(String userId, int amount);

    String status();

    void ping();
}
