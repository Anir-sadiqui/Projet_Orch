package com.membership.users.infrastructure.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);


    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        log.warn("Ressource non trouvée: {}", ex.getMessage());
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
    }


    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleConflict(
            ResourceAlreadyExistsException ex,
            HttpServletRequest request) {

        log.warn("Ressource déjà existante: {}", ex.getMessage());
        return buildError(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI());
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        log.warn("Erreur de validation");

        List<ErrorResponse.ValidationError> validationErrors =
                ex.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .map(this::toValidationError)
                        .toList();

        return buildError(
                HttpStatus.BAD_REQUEST,
                "Erreur de validation des données",
                request.getRequestURI(),
                validationErrors
        );
    }


    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {

        String message = String.format(
                "Le paramètre '%s' avec la valeur '%s' n'est pas du type attendu",
                ex.getName(), ex.getValue()
        );

        log.warn("Erreur de type de paramètre: {}", message);
        return buildError(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
    }


    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        log.warn("Argument illégal: {}", ex.getMessage());
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobal(
            Exception ex,
            HttpServletRequest request) {

        log.error("Erreur interne du serveur", ex);

        return buildError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Une erreur interne s'est produite. Veuillez contacter l'administrateur.",
                request.getRequestURI()
        );
    }


    private ResponseEntity<ErrorResponse> buildError(
            HttpStatus status,
            String message,
            String path) {

        return buildError(status, message, path, null);
    }

    private ResponseEntity<ErrorResponse> buildError(
            HttpStatus status,
            String message,
            String path,
            List<ErrorResponse.ValidationError> validationErrors) {

        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(path)
                .validationErrors(validationErrors)
                .build();

        return ResponseEntity.status(status).body(error);
    }

    private ErrorResponse.ValidationError toValidationError(FieldError error) {
        return ErrorResponse.ValidationError.builder()
                .field(error.getField())
                .message(error.getDefaultMessage())
                .rejectedValue(error.getRejectedValue())
                .build();
    }
}
