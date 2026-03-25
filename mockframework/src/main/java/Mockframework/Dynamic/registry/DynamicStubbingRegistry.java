package Mockframework.Dynamic.registry;

import Mockframework.Core.Answer;
import Mockframework.Core.matcher.ArgumentMatcher;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;

public final class DynamicStubbingRegistry {
    private static final DynamicStubbingRegistry INSTANCE = new DynamicStubbingRegistry();

    private final Map<InvocationKey, Answer> stubs = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<MatcherStub> matcherStubs = new CopyOnWriteArrayList<>();
    private final ThreadLocal<InvocationKey> lastInvocation = new ThreadLocal<>();
    private final ThreadLocal<List<ArgumentMatcher>> pendingMatchers =
        ThreadLocal.withInitial(ArrayList::new);

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

    public void addMatcherStub(InvocationKey key, List<ArgumentMatcher> argumentMatchers, Answer answer) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(argumentMatchers, "argumentMatchers");
        Objects.requireNonNull(answer, "answer");
        MatcherStub newStub = MatcherStub.create(key, argumentMatchers, answer);
        matcherStubs.removeIf(existing -> existing.hasSamePattern(newStub));
        matcherStubs.add(newStub);
    }

    public Answer getStub(InvocationKey key) {
        Answer exact = stubs.get(key);
        if (exact != null) {
            return exact;
        }
        for (int i = matcherStubs.size() - 1; i >= 0; i--) {
            MatcherStub matcherStub = matcherStubs.get(i);
            if (matcherStub.matches(key)) {
                return matcherStub.answer;
            }
        }
        return null;
    }

    public void registerMatcher(ArgumentMatcher matcher) {
        pendingMatchers.get().add(Objects.requireNonNull(matcher, "matcher"));
    }

    public List<ArgumentMatcher> consumeMatchers() {
        List<ArgumentMatcher> current = pendingMatchers.get();
        if (current.isEmpty()) {
            pendingMatchers.remove();
            return List.of();
        }
        List<ArgumentMatcher> copy = List.copyOf(current);
        pendingMatchers.remove();
        return copy;
    }

    public void reset() {
        stubs.clear();
        matcherStubs.clear();
        lastInvocation.remove();
        pendingMatchers.remove();
        clearHistory();
    }

    private static final class MatcherStub {
        private final Object mock;
        private final Method method;
        private final List<ArgumentMatcher> matchers;
        private final Answer answer;

        private MatcherStub(Object mock, Method method, List<ArgumentMatcher> matchers, Answer answer) {
            this.mock = mock;
            this.method = method;
            this.matchers = matchers;
            this.answer = answer;
        }

        private static MatcherStub create(InvocationKey key, List<ArgumentMatcher> matchers, Answer answer) {
            return new MatcherStub(
                key.getMock(),
                key.getMethod(),
                List.copyOf(matchers),
                answer
            );
        }

        private boolean hasSamePattern(MatcherStub other) {
            return mock == other.mock
                && method.equals(other.method)
                && matchers.equals(other.matchers);
        }

        private boolean matches(InvocationKey key) {
            if (mock != key.getMock() || !method.equals(key.getMethod())) {
                return false;
            }

            Object[] args = key.getArgs();
            if (args.length != matchers.size()) {
                return false;
            }
            for (int i = 0; i < args.length; i++) {
                if (!matchers.get(i).matches(args[i])) {
                    return false;
                }
            }
            return true;
        }
    }
    // В начало класса, после других полей:
    private final Map<Object, List<InvocationKey>> invocationHistory = new ConcurrentHashMap<>();

    // В конце класса добавить методы:
    public void recordInvocation(InvocationKey key) {
        invocationHistory.computeIfAbsent(key.getMock(), k -> new CopyOnWriteArrayList<>()).add(key);
    }

    public List<InvocationKey> getHistory(Object mock) {
        if (mock == null) {
            System.out.println("History size: 0");
            return Collections.emptyList();
        }
        List<InvocationKey> history = invocationHistory.getOrDefault(mock, Collections.emptyList());
        System.out.println("History size: " + history.size());
        return history;
    }

    public void clearHistory() {
        invocationHistory.clear();
    }

    public void dropInvocationFromHistory(InvocationKey key) {
        if (key == null) {
            return;
        }
        List<InvocationKey> history = invocationHistory.get(key.getMock());
        if (history == null || history.isEmpty()) {
            return;
        }
        for (int i = history.size() - 1; i >= 0; i--) {
            if (history.get(i).equals(key)) {
                history.remove(i);
                break;
            }
        }
        if (history.isEmpty()) {
            invocationHistory.remove(key.getMock());
        }
    }

    public List<ArgumentMatcher> consumeMatchersForVerification() {
        List<ArgumentMatcher> current = pendingMatchers.get();
        if (current.isEmpty()) {
            pendingMatchers.remove();
            return List.of();
        }
        List<ArgumentMatcher> copy = List.copyOf(current);
        pendingMatchers.remove();
        return copy;
    }
}
