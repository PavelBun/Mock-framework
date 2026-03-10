package ru.pozhidaev.mockframework.core;

@FunctionalInterface
public interface Answer {
    Object answer(Object[] args) throws Throwable;
}

