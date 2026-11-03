package io.github.ragportfolio.adapters;

import io.github.ragportfolio.adapters.http.AnswerController;
import io.github.ragportfolio.adapters.http.ApiExceptionHandler;
import io.github.ragportfolio.adapters.http.StrictJsonTypes;
import io.github.ragportfolio.application.AdapterNotImplementedException;
import io.github.ragportfolio.application.AnswerResult;
import io.github.ragportfolio.application.AskQuestionCommand;
import io.github.ragportfolio.application.AskQuestionUseCase;
import io.github.ragportfolio.application.GenerationFailedException;
import io.github.ragportfolio.domain.Actor;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AnswerControllerTest {
    private static final Actor DEMO = new Actor("demo-tenant", "demo-user");
    private static final AnswerResult RESULT = new AnswerResult(AnswerResult.Outcome.MOCK_ANSWER, "[MOCK] fixture", List.of(), "MOCK");

    @ParameterizedTest
    @ValueSource(strings = {"{\"question\":\"fixture question\"}", "{\"question\":\"fixture question\",\"topK\":null}"})
    void mapsValidatedRequestAndIgnoresUntrustedIdentityHeaders(String body) throws Exception {
        var received = new AtomicReference<AskQuestionCommand>();
        var mvc = mvc(command -> { received.set(command); return RESULT; });
        mvc.perform(post("/api/v1/answers").contentType(MediaType.APPLICATION_JSON)
                .header("X-Tenant-Id", "attacker").header("X-User-Id", "attacker")
                .content(body))
                .andExpect(status().isOk()).andExpect(jsonPath("$.outcome").value("MOCK_ANSWER"))
                .andExpect(jsonPath("$.generationMode").value("MOCK"));
        assertEquals(DEMO, received.get().actor());
        assertEquals(5, received.get().topK());
    }

    @Test void rejectsInvalidInputAndMalformedJson() throws Exception {
        var mvc = mvc(command -> { throw new AssertionError("invalid request reached use case"); });
        for (String body : List.of("{\"question\":\" \"}", "{\"question\":\"q\",\"topK\":0}",
                "{\"question\":\"q\",\"topK\":21}", "{\"question\":\"" + "a".repeat(4001) + "\"}", "{broken")) {
            mvc.perform(post("/api/v1/answers").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
        }
    }

    @Test void reportsUnimplementedAndProviderFailuresWithoutCauseContent() throws Exception {
        mvc(command -> { throw new AdapterNotImplementedException("secret fixture name"); })
                .perform(post("/api/v1/answers").contentType(MediaType.APPLICATION_JSON).content("{\"question\":\"q\"}"))
                .andExpect(status().isNotImplemented()).andExpect(jsonPath("$.code").value("ADAPTER_NOT_IMPLEMENTED"));
        mvc(command -> { throw new GenerationFailedException(new IllegalStateException("secret provider body")); })
                .perform(post("/api/v1/answers").contentType(MediaType.APPLICATION_JSON).content("{\"question\":\"q\"}"))
                .andExpect(status().isBadGateway()).andExpect(content().json("{\"code\":\"GENERATION_FAILED\",\"message\":\"Answer generation failed.\"}"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{\"question\":\"q\",\"topK\":1.9}",
            "{\"question\":123,\"topK\":5}", "{\"question\":\"q\",\"topK\":\"5\"}"})
    void rejectsJsonTypesThatDoNotMatchTheContract(String body) throws Exception {
        mvc(command -> RESULT).perform(post("/api/v1/answers")
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    private static MockMvc mvc(AskQuestionUseCase useCase) {
        var mapper = JsonMapper.builder();
        StrictJsonTypes.configure(mapper);
        return MockMvcBuilders.standaloneSetup(new AnswerController(useCase, DEMO))
                .setMessageConverters(new JacksonJsonHttpMessageConverter(mapper.build()))
                .setControllerAdvice(new ApiExceptionHandler()).build();
    }
}
