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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClientUnicityLicenseValidatorStep - Tests unitaires")
class ClientUnicityLicenseValidatorStepTest {

    @Mock
    private ClientStorageProvider repository;

    private ClientUnicityLicenseValidatorStep validator;

    @BeforeEach
    void setUp() {
        validator = new ClientUnicityLicenseValidatorStep(repository);
    }

    @Test
    @DisplayName("Doit rejeter si le numéro de permis existe déjà")
    void shouldThrowConflictWhenLicenseExists() {
        // Given
        String existingLicense = "AB123YZ";
        Client clientToCheck = Client.builder().licenseNumber(existingLicense).build();

        // Mock : Le repository trouve un client existant
        when(repository.findByLicenseNumber(existingLicense))
                .thenReturn(Optional.of(Client.builder().build()));

        // When & Then
        assertThatThrownBy(() -> validator.check(clientToCheck))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("Doit accepter si le numéro de permis est nouveau")
    void shouldAcceptWhenLicenseIsNew() {
        // Given
        String newLicense = "YZ999AB";
        Client clientToCheck = Client.builder().licenseNumber(newLicense).build();

        // Mock : Le repository ne trouve rien
        when(repository.findByLicenseNumber(newLicense))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatCode(() -> validator.check(clientToCheck))
                .doesNotThrowAnyException();
    }
}