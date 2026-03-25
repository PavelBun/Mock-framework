package Mockframework.Static.verification;

import Mockframework.Core.matcher.ArgumentMatcher;

import java.util.Arrays;
import java.util.List;

public class AtMost implements VerificationMode {
    private final int max;

    public AtMost(int max) {
        this.max = max;
    }

    @Override
    public void verify(List<StaticInvocationKey> history, StaticInvocationKey expected, List<ArgumentMatcher> matchers) {
        long actualCount = history.stream()
            .filter(key -> matches(expected, key, matchers))
            .count();
        if (actualCount > max) {
            throw new AssertionError(
                String.format("Expected at most %d invocations, but got %d", max, actualCount)
            );
        }
    }

    private boolean matches(
        StaticInvocationKey expected,
        StaticInvocationKey actual,
        List<ArgumentMatcher> matchers
    ) {
        if (!expected.getClazz().equals(actual.getClazz())) {
            return false;
        }
        if (!expected.getMethodSignature().equals(actual.getMethodSignature())) {
            return false;
        }
        if (matchers == null || matchers.isEmpty()) {
            return Arrays.deepEquals(expected.getArgs(), actual.getArgs());
        }

        Object[] actualArgs = actual.getArgs();
        if (actualArgs.length != matchers.size()) {
            return false;
        }
        for (int i = 0; i < actualArgs.length; i++) {
            if (!matchers.get(i).matches(actualArgs[i])) {
                return false;
            }
        }
        return true;
    }
}
