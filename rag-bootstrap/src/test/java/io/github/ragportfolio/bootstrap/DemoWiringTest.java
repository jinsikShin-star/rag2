package io.github.ragportfolio.bootstrap;

import io.github.ragportfolio.adapters.http.AnswerController;
import io.github.ragportfolio.application.AskQuestionCommand;
import io.github.ragportfolio.application.AskQuestionUseCase;
import io.github.ragportfolio.domain.Actor;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import tools.jackson.databind.exc.MismatchedInputException;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.*;

class DemoWiringTest {
    @Test void bootMapperAppliesTheSameStrictJsonTypesAsStandaloneHttpTests() {
        new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(JacksonAutoConfiguration.class))
                .withInitializer(context -> context.getEnvironment().setActiveProfiles("demo"))
                .withUserConfiguration(DemoConfiguration.class).run(context -> {
                    assertNull(context.getStartupFailure());
                    var mapper = context.getBean(JsonMapper.class);
                    for (String body : List.of("{\"question\":\"q\",\"topK\":1.9}",
                            "{\"question\":123,\"topK\":5}", "{\"question\":\"q\",\"topK\":\"5\"}")) {
                        assertThrows(MismatchedInputException.class,
                                () -> mapper.readValue(body, AnswerController.AnswerRequest.class));
                    }
                    assertEquals(5, mapper.readValue("{\"question\":\"q\",\"topK\":5}",
                            AnswerController.AnswerRequest.class).topK());
                });
    }

    @Test void defaultProfileDoesNotWireDemoApiOrMockUseCase() {
        try (var context = new AnnotationConfigApplicationContext()) {
            context.register(DemoConfiguration.class);
            context.refresh();
            assertTrue(context.getBeansOfType(AskQuestionUseCase.class).isEmpty());
            assertTrue(context.getBeansOfType(AnswerController.class).isEmpty());
        }
    }

    @Test void explicitDemoProfileWiresOnlyLocalFixtureFlowWithoutSockets() {
        try (var context = new AnnotationConfigApplicationContext()) {
            context.getEnvironment().setActiveProfiles("demo");
            context.register(DemoConfiguration.class);
            context.refresh();
            assertNotNull(context.getBean(AnswerController.class));
            var actor = context.getBean(Actor.class);
            assertEquals(new Actor("demo-tenant", "demo-user"), actor);
            var result = context.getBean(AskQuestionUseCase.class).ask(new AskQuestionCommand(actor, "fixture question", 5));
            assertEquals("MOCK", result.generationMode());
            assertEquals("demo-document", result.citations().getFirst().documentId());
            assertTrue(context.getBeansOfType(io.github.ragportfolio.adapters.external.ExternalLlmAdapter.class).isEmpty());
        }
    }
}
