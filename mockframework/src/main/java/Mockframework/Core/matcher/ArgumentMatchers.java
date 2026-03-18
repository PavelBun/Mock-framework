package Mockframework.Core.matcher;

import java.util.Objects;

public final class ArgumentMatchers {
    private static final ArgumentMatcher ANY = new ArgumentMatcher() {
        @Override
        public boolean matches(Object argument) {
            return true;
        }

        @Override
        public boolean equals(Object other) {
            return this == other;
        }

        @Override
        public int hashCode() {
            return 1;
        }
    };

    private ArgumentMatchers() {
    }

    public static ArgumentMatcher any() {
        return ANY;
    }

    public static ArgumentMatcher eq(Object expected) {
        return new EqMatcher(expected);
    }

    public static ArgumentMatcher contains(String expectedPart) {
        return new ContainsMatcher(expectedPart);
    }

    private static final class EqMatcher implements ArgumentMatcher {
        private final Object expected;

        private EqMatcher(Object expected) {
            this.expected = expected;
        }

        @Override
        public boolean matches(Object argument) {
            return Objects.deepEquals(expected, argument);
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof EqMatcher that)) {
                return false;
            }
            return Objects.deepEquals(expected, that.expected);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(expected);
        }
    }

    private static final class ContainsMatcher implements ArgumentMatcher {
        private final String expectedPart;

        private ContainsMatcher(String expectedPart) {
            this.expectedPart = Objects.requireNonNull(expectedPart, "expectedPart must not be null");
        }

        @Override
        public boolean matches(Object argument) {
            return argument instanceof String value && value.contains(expectedPart);
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof ContainsMatcher that)) {
                return false;
            }
            return expectedPart.equals(that.expectedPart);
        }

        @Override
        public int hashCode() {
            return expectedPart.hashCode();
        }
    }
}
