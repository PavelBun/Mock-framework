package Mockframework.Static;

import Mockframework.Core.Answer;

public interface OngoingStubbing<R> {
    OngoingStubbing<R> thenReturn(R value);

    OngoingStubbing<R> thenThrow(Throwable throwable);

    OngoingStubbing<R> thenAnswer(Answer answer);
}
