package com.imt.clients.validators;

import com.imt.clients.ClientStorageProvider;
import com.imt.clients.model.Client;
import com.imt.common.exceptions.ConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClientUnicityValidatorStep - Tests unitaires")
class ClientUnicityValidatorStepTest {

    @Mock
    private ClientStorageProvider repository;

    private ClientUnicityValidatorStep validator;

    @BeforeEach
    void setUp() {
        validator = new ClientUnicityValidatorStep(repository);
    }

    @Test
    @DisplayName("Doit rejeter si la combinaison Nom/Prénom/Date existe déjà")
    void shouldThrowConflictWhenClientExists() {
        // Given
        Client clientToCheck = Client.builder()
                .lastName("Dupont")
                .firstName("Jean")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .build();

        // Mock : Trouvé
        when(repository.findByLastNameAndFirstNameAndBirthDate(
                "Dupont", "Jean", LocalDate.of(1990, 1, 1)))
                .thenReturn(Optional.of(Client.builder().build()));

        // When & Then
        assertThatThrownBy(() -> validator.check(clientToCheck))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("Doit accepter si le client est unique")
    void shouldAcceptWhenClientIsUnique() {
        // Given
        Client clientToCheck = Client.builder()
                .lastName("Inconnu")
                .firstName("Nouveau")
                .dateOfBirth(LocalDate.now())
                .build();

        // Mock : Pas trouvé
        when(repository.findByLastNameAndFirstNameAndBirthDate(any(), any(), any()))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatCode(() -> validator.check(clientToCheck))
                .doesNotThrowAnyException();
    }
}