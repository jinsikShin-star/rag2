package io.github.ragportfolio.bootstrap;

import io.github.ragportfolio.adapters.http.AnswerController;
import io.github.ragportfolio.adapters.http.ApiExceptionHandler;
import io.github.ragportfolio.adapters.http.StrictJsonTypes;
import io.github.ragportfolio.adapters.observability.LoggingObservationAdapter;
import io.github.ragportfolio.adapters.stub.DemoAccessPolicy;
import io.github.ragportfolio.adapters.stub.FixtureDocumentCatalog;
import io.github.ragportfolio.adapters.stub.FixtureRetrievalAdapter;
import io.github.ragportfolio.adapters.stub.MockGenerationAdapter;
import io.github.ragportfolio.application.AskQuestionUseCase;
import io.github.ragportfolio.application.QuestionService;
import io.github.ragportfolio.domain.Actor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;

@Configuration(proxyBeanMethods = false)
@Profile("demo")
@Import({AnswerController.class, ApiExceptionHandler.class})
public class DemoConfiguration {
    @Bean
    JsonMapperBuilderCustomizer strictJsonTypes() {
        return StrictJsonTypes::configure;
    }

    @Bean
    Actor demoActor() {
        return new Actor("demo-tenant", "demo-user");
    }

    @Bean
    AskQuestionUseCase questions() {
        return new QuestionService(new FixtureRetrievalAdapter(), new MockGenerationAdapter(),
                new FixtureDocumentCatalog(), new DemoAccessPolicy(), new LoggingObservationAdapter());
    }
}
