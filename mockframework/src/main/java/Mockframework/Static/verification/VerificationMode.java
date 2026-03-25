package Mockframework.Static.verification;

import Mockframework.Core.matcher.ArgumentMatcher;

import java.util.List;

public interface VerificationMode {
    void verify(
        List<StaticInvocationKey> history,
        StaticInvocationKey expected,
        List<ArgumentMatcher> matchers
    );
}
