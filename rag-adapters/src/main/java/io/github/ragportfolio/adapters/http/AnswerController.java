package io.github.ragportfolio.adapters.http;

import io.github.ragportfolio.application.AnswerResult;
import io.github.ragportfolio.application.AskQuestionCommand;
import io.github.ragportfolio.application.AskQuestionUseCase;
import io.github.ragportfolio.domain.Actor;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Profile("demo")
@RestController
public final class AnswerController {
    private final AskQuestionUseCase questions;
    private final Actor demoActor;

    public AnswerController(AskQuestionUseCase questions, Actor demoActor) {
        this.questions = questions;
        this.demoActor = demoActor;
    }

    public record AnswerRequest(@NotBlank @Size(max = 4000) String question,
            @Min(1) @Max(20) Integer topK) { }

    @PostMapping("/api/v1/answers")
    public AnswerResult answer(@Valid @RequestBody AnswerRequest request) {
        return questions.ask(new AskQuestionCommand(demoActor, request.question(),
                request.topK() == null ? 5 : request.topK()));
    }
}
