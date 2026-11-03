package io.github.ragportfolio.application;

public interface AskQuestionUseCase {
    AnswerResult ask(AskQuestionCommand command);
}
