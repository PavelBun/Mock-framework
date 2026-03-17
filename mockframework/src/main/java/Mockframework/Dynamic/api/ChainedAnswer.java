package Mockframework.Dynamic.api;

import Mockframework.Core.Answer;
import java.util.List;

public class ChainedAnswer implements Answer {
    private final List<Answer> answers;
    private int invocationCount = 0;

    public ChainedAnswer(List<Answer> answers) {
        this.answers = answers;
    }

    @Override
    public Object answer(Object[] args) throws Throwable {
        int index = invocationCount;
        invocationCount++;
        if (index >= answers.size()) {
            index = answers.size() - 1;
        }
        return answers.get(index).answer(args);
    }
}