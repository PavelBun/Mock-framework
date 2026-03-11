package Mockframework.Dynamic.api;

import Mockframework.Core.Answer;

public interface DynamicOngoingStubbing<R> {
    DynamicOngoingStubbing<R> thenReturn(R value);
    DynamicOngoingStubbing<R> thenThrow(Throwable throwable);
    DynamicOngoingStubbing<R> thenAnswer(Answer answer);
}