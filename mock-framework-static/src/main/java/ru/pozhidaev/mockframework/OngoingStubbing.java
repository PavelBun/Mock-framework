package ru.pozhidaev.mockframework;

import ru.pozhidaev.mockframework.core.Answer;

public interface OngoingStubbing<R> {
    void thenReturn(R value);

    void thenThrow(Throwable throwable);

    void thenAnswer(Answer answer);
}

