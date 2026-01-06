package com.imt.common.validators;

import com.imt.common.exceptions.BadRequestException;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Tests unitaires pour la classe ConstraintValidatorStep.
 * Vérifie que le moteur de validation (Hibernate Validator) est bien déclenché
 * et que les exceptions correctes sont levées.
 */
@DisplayName("ConstraintValidatorStep - Tests unitaires")
class ConstraintValidatorStepTest {

    // On teste avec un objet 'TestObject' dédié (défini plus bas) pour ne pas dépendre du modèle Client
    private ConstraintValidatorStep<TestObject> validator;

    @BeforeEach
    void setUp() {
        validator = new ConstraintValidatorStep<>();
    }

    @Nested
    @DisplayName("check() - Validation des contraintes")
    class CheckTests {

        @Test
        @DisplayName("Doit accepter un objet valide")
        void shouldAcceptValidObject() {
            // Given
            TestObject validObject = TestObject.builder()
                    .name("John")
                    .email("john@example.com")
                    .age(25)
                    .build();

            // When & Then
            assertDoesNotThrow(() -> validator.check(validObject));
        }

        @Test
        @DisplayName("Doit rejeter un objet avec un champ null annoté @NotNull")
        void shouldRejectObjectWithNullNotNullField() {
            // Given
            TestObject invalidObject = TestObject.builder()
                    .name(null) // Violation @NotNull
                    .email("john@example.com")
                    .age(25)
                    .build();

            // When & Then
            assertThatThrownBy(() -> validator.check(invalidObject))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("At least one constraint violation found")
                    .hasMessageContaining("Le nom ne peut pas être nul");
        }

        @Test
        @DisplayName("Doit rejeter un objet avec un pattern invalide")
        void shouldRejectObjectWithInvalidPattern() {
            // Given
            TestObject invalidObject = TestObject.builder()
                    .name("John")
                    .email("invalid-email") // Violation @Pattern
                    .age(25)
                    .build();

            // When & Then
            assertThatThrownBy(() -> validator.check(invalidObject))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("L'email n'est pas valide");
        }

        @Test
        @DisplayName("Doit rejeter un objet avec une taille invalide")
        void shouldRejectObjectWithInvalidSize() {
            // Given
            TestObject invalidObject = TestObject.builder()
                    .name("Jo") // Violation @Size (min=3)
                    .email("john@example.com")
                    .age(25)
                    .build();

            // When & Then
            assertThatThrownBy(() -> validator.check(invalidObject))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Le nom doit contenir entre 3 et 50 caractères");
        }
    }

    // --- Objet de test interne (Stub) ---
    // Cela permet de tester le validateur sans dépendre de vos vrais objets métier (Client, Vehicle...)

    @Getter
    @Builder
    @AllArgsConstructor
    static class TestObject {
        @NotNull(message = "Le nom ne peut pas être nul")
        @Size(min = 3, max = 50, message = "Le nom doit contenir entre 3 et 50 caractères")
        private String name;

        @NotNull(message = "L'email ne peut pas être nul")
        @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
                message = "L'email n'est pas valide")
        private String email;

        private Integer age;
    }
}