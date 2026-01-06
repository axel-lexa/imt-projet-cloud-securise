package com.imt.adaptersinrest.common.model.output;


import com.imt.common.exceptions.BadRequestException;
import com.imt.common.exceptions.ConflictException;
import com.imt.common.exceptions.ImtException;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * 🚨 Gestion globale des erreurs (style de l'enseignant).
 * Capture les ImtException et les exceptions génériques.
 */
@RestControllerAdvice
public class ControllerExceptionHandler {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Gère les erreurs métier (BadRequest, Conflict)
     */
    @ExceptionHandler({BadRequestException.class, ConflictException.class})
    public ResponseEntity<ExceptionOutput> handleImtException(final HttpServletRequest request, final ImtException exception) {

        // Détermine le statut HTTP en fonction du type d'exception
        HttpStatus status = exception instanceof ConflictException
                ? HttpStatus.CONFLICT // 409
                : HttpStatus.BAD_REQUEST; // 400

        return new ResponseEntity<>(
                buildExceptionOutput(request, exception, exception.getType()),
                status
        );
    }

    /**
     * Gère toutes les autres exceptions (erreurs 500)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionOutput> handleGenericException(final HttpServletRequest request, final Exception exception) {
        return new ResponseEntity<>(
                buildExceptionOutput(request, exception, exception.getClass().getSimpleName()),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    private ExceptionOutput buildExceptionOutput(HttpServletRequest request, Exception exception, String type) {
        return ExceptionOutput.builder()
                .type(type)
                .message(exception.getMessage())
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .path(request.getRequestURI() + Optional.ofNullable(request.getQueryString()).map(query -> "?" + query).orElse(Strings.EMPTY))
                .build();
    }
}
