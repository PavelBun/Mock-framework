package Mockframework.Dynamic.registry;

import Mockframework.Core.Answer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class DynamicStubbingRegistry {
    private static final DynamicStubbingRegistry INSTANCE = new DynamicStubbingRegistry();

    private final Map<InvocationKey, Answer> stubs = new ConcurrentHashMap<>();
    private final ThreadLocal<InvocationKey> lastInvocation = new ThreadLocal<>();

    private DynamicStubbingRegistry() {}

    public static DynamicStubbingRegistry getInstance() {
        return INSTANCE;
    }

    public void recordLastInvocation(InvocationKey key) {
        lastInvocation.set(key);
    }

    public InvocationKey getLastInvocationAndClear() {
        InvocationKey key = lastInvocation.get();
        lastInvocation.remove();
        return key;
    }

    public void addStub(InvocationKey key, Answer answer) {
        stubs.put(key, answer);
    }

    public Answer getStub(InvocationKey key) {
        return stubs.get(key);
    }

    public void reset() {
        stubs.clear();
        lastInvocation.remove();
    }
}