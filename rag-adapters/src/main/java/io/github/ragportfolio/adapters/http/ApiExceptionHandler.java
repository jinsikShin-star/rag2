package io.github.ragportfolio.adapters.http;

import io.github.ragportfolio.application.AdapterNotImplementedException;
import io.github.ragportfolio.application.GenerationFailedException;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Profile("demo")
@RestControllerAdvice
public final class ApiExceptionHandler {
    public record ApiError(String code, String message) { }

    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class,
            IllegalArgumentException.class})
    public ResponseEntity<ApiError> invalidRequest(Exception ignored) {
        return ResponseEntity.badRequest().body(new ApiError("INVALID_REQUEST", "Invalid question or topK."));
    }

    @ExceptionHandler(AdapterNotImplementedException.class)
    public ResponseEntity<ApiError> notImplemented(AdapterNotImplementedException ignored) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(new ApiError("ADAPTER_NOT_IMPLEMENTED", "The requested external adapter is not implemented."));
    }

    @ExceptionHandler(GenerationFailedException.class)
    public ResponseEntity<ApiError> generationFailure(GenerationFailedException ignored) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new ApiError("GENERATION_FAILED", "Answer generation failed."));
    }
}
