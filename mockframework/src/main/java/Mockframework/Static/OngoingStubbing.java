package Mockframework.Static;

import Mockframework.Core.Answer;

public interface OngoingStubbing<R> {
    void thenReturn(R value);

    void thenThrow(Throwable throwable);

    void thenAnswer(Answer answer);
}

