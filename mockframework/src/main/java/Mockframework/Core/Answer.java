package Mockframework.Core;

@FunctionalInterface
public interface Answer {
    Object answer(Object[] args) throws Throwable;
}

