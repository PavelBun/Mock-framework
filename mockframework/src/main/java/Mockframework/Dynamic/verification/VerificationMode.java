package Mockframework.Dynamic.verification;

import Mockframework.Core.matcher.ArgumentMatcher;
import Mockframework.Dynamic.registry.InvocationKey;

import java.util.List;

public interface VerificationMode {
    void verify(List<InvocationKey> history, InvocationKey expected, List<ArgumentMatcher> matchers);
}