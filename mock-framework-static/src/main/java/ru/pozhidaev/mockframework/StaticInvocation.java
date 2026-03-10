package ru.pozhidaev.mockframework;

import java.io.Serializable;

@FunctionalInterface
public interface StaticInvocation<R> extends Serializable {
    R invoke() throws Throwable;
}
