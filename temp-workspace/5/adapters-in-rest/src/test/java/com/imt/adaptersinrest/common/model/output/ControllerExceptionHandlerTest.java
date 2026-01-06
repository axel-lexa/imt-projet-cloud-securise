package com.imt.adaptersinrest.common.model.output;

import com.imt.common.exceptions.BadRequestException;
import com.imt.common.exceptions.ConflictException;
import com.imt.common.exceptions.ImtException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour le gestionnaire global d'exceptions ControllerExceptionHandler.
 */
@ExtendWith(MockitoExtension.class)
class ControllerExceptionHandlerTest {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final String TEST_URI = "/api/v1/clients";
    private static final String TEST_QUERY_STRING = "param1=value1";

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private ControllerExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        // Configuration par défaut du mock request
        lenient().when(request.getRequestURI()).thenReturn(TEST_URI);
    }

    // ==================== Tests handleImtException (Erreurs Métier) ====================

    @Test
    @DisplayName("Doit retourner 400 BAD REQUEST pour une BadRequestException")
    void handleImtException_WithBadRequest_ShouldReturn400() {
        // Arrange
        String errorMessage = "Données invalides";
        ImtException exception = new BadRequestException(errorMessage);
        when(request.getQueryString()).thenReturn(null);

        // Act
        ResponseEntity<ExceptionOutput> response = exceptionHandler.handleImtException(request, exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        ExceptionOutput body = response.getBody();
        assertNotNull(body);
        assertEquals("BadRequestException", body.getType());
        assertEquals(errorMessage, body.getMessage());
        assertEquals(TEST_URI, body.getPath());
        assertNotNull(body.getTimestamp());
    }

    @Test
    @DisplayName("Doit retourner 409 CONFLICT pour une ConflictException")
    void handleImtException_WithConflict_ShouldReturn409() {
        // Arrange
        String errorMessage = "Ce client existe déjà";
        ImtException exception = new ConflictException(errorMessage);
        when(request.getQueryString()).thenReturn(null);

        // Act
        ResponseEntity<ExceptionOutput> response = exceptionHandler.handleImtException(request, exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode()); // Vérifie la logique spécifique de votre handler

        ExceptionOutput body = response.getBody();
        assertNotNull(body);
        assertEquals("ConflictException", body.getType());
        assertEquals(errorMessage, body.getMessage());
    }

    @Test
    @DisplayName("Doit inclure la QueryString dans le chemin si elle existe")
    void handleImtException_WithQueryString_ShouldIncludeItInPath() {
        // Arrange
        ImtException exception = new BadRequestException("Erreur");
        when(request.getQueryString()).thenReturn(TEST_QUERY_STRING);

        // Act
        ResponseEntity<ExceptionOutput> response = exceptionHandler.handleImtException(request, exception);

        // Assert
        assertNotNull(response.getBody());
        // Vérifie que "?param1=value1" est bien ajouté à l'URI
        assertEquals(TEST_URI + "?" + TEST_QUERY_STRING, response.getBody().getPath());
    }

    // ==================== Tests handleGenericException (Erreurs Serveur) ====================

    @Test
    @DisplayName("Doit retourner 500 INTERNAL SERVER ERROR pour une Exception générique")
    void handleGenericException_ShouldReturn500() {
        // Arrange
        String errorMessage = "Null Pointer quelque part";
        Exception exception = new NullPointerException(errorMessage);
        when(request.getQueryString()).thenReturn(null);

        // Act
        ResponseEntity<ExceptionOutput> response = exceptionHandler.handleGenericException(request, exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        ExceptionOutput body = response.getBody();
        assertNotNull(body);
        assertEquals("NullPointerException", body.getType());
        assertEquals(errorMessage, body.getMessage());
    }

    @Test
    @DisplayName("Doit générer un timestamp valide et récent")
    void handleGenericException_ShouldGenerateRecentTimestamp() {
        // Arrange
        Exception exception = new RuntimeException("Erreur");
        LocalDateTime before = LocalDateTime.now();

        // Act
        ResponseEntity<ExceptionOutput> response = exceptionHandler.handleGenericException(request, exception);

        // Assert
        LocalDateTime after = LocalDateTime.now();
        assertNotNull(response.getBody());

        // Parsing du timestamp renvoyé par le handler
        LocalDateTime timestamp = LocalDateTime.parse(response.getBody().getTimestamp(), FORMATTER);

        // Vérifie que le timestamp est bien compris dans l'intervalle d'exécution du test
        assertTrue(timestamp.isAfter(before.minusSeconds(1)) || timestamp.isEqual(before));
        assertTrue(timestamp.isBefore(after.plusSeconds(1)) || timestamp.isEqual(after));
    }
}