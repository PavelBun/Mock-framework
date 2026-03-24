package Mockframework.Dynamic.verification;

import Mockframework.Core.matcher.ArgumentMatcher;
import Mockframework.Dynamic.registry.InvocationKey;

import java.util.Arrays;
import java.util.List;

public class AtMost implements VerificationMode {
    private final int max;

    public AtMost(int max) {
        this.max = max;
    }

    @Override
    public void verify(List<InvocationKey> history, InvocationKey expected, List<ArgumentMatcher> matchers) {
        long actualCount = history.stream()
                .filter(key -> matches(expected, key, matchers))
                .count();
        if (actualCount > max) {
            throw new AssertionError(
                    String.format("Expected at most %d invocations, but got %d", max, actualCount)
            );
        }
    }

    private boolean matches(InvocationKey expected, InvocationKey actual, List<ArgumentMatcher> matchers) {
        if (!expected.getMethod().equals(actual.getMethod())) {
            return false;
        }
        if (matchers.isEmpty()) {
            return Arrays.deepEquals(expected.getArgs(), actual.getArgs());
        } else {
            Object[] expectedArgs = expected.getArgs();
            Object[] actualArgs = actual.getArgs();
            if (expectedArgs.length != matchers.size()) {
                return false;
            }
            for (int i = 0; i < expectedArgs.length; i++) {
                if (!matchers.get(i).matches(actualArgs[i])) {
                    return false;
                }
            }
            return true;
        }
    }
}