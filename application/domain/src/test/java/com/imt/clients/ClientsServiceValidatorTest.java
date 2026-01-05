package com.imt.clients;

import com.imt.clients.model.Client;
import com.imt.common.exceptions.ConflictException;
import com.imt.common.exceptions.ImtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClientsServiceValidator - Tests d'intégration du domaine")
class ClientsServiceValidatorTest {

    @Mock
    private ClientStorageProvider repository;

    private ClientsServiceValidator service;

    @BeforeEach
    void setUp() {
        // On injecte le mock du repository dans le service
        service = new ClientsServiceValidator(repository);
    }

    @Test
    @DisplayName("CREATE - Doit créer le client quand tout est valide")
    void shouldCreateClientWhenAllValid() throws ImtException {
        // Given
        Client newClient = Client.builder()
                .id(UUID.randomUUID().toString())
                .lastName("Valid")
                .firstName("User")
                .dateOfBirth(LocalDate.of(2000, 1, 1))
                .licenseNumber("010203040506123")
                .address("123 Rue Test")
                .build();

        // Mock des vérifications d'unicité (rien n'existe)
        when(repository.findByLastNameAndFirstNameAndBirthDate(any(), any(), any()))
                .thenReturn(Optional.empty());
        when(repository.findByLicenseNumber(any()))
                .thenReturn(Optional.empty());

        // Mock de la sauvegarde
        when(repository.save(any(Client.class))).thenReturn(newClient);

        // When
        Client created = service.create(newClient);

        // Then
        assertThat(created).isNotNull();
        verify(repository).save(newClient); // Vérifie que save() a été appelé
    }


    @Test
    @DisplayName("CREATE - Doit échouer si le permis existe déjà")
    void shouldNotCreateClientWhenLicenseExists() {
        // Given
        String existingLicense = "010203040506123"; // Une valeur VALIDE pour le pattern, mais DÉJÀ PRISE

        Client invalidClient = Client.builder()
                .lastName("Valid")
                .firstName("User")
                .dateOfBirth(LocalDate.of(2000, 1, 1))
                .licenseNumber(existingLicense)
                .address("123 Rue Test")
                .build();

        // Mock : On dit "Si on cherche '010203040506123', on trouve quelqu'un"
        when(repository.findByLicenseNumber(existingLicense))
                .thenReturn(Optional.of(Client.builder().build()));

        // When & Then
        // 1. La ConstraintValidatorStep va passer (car "010203040506123" respecte le pattern)
        // 2. La ClientUnicityLicenseValidatorStep va échouer (car le mock dit qu'il existe)
        assertThatThrownBy(() -> service.create(invalidClient))
                .isInstanceOf(ConflictException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("UPDATE - Doit mettre à jour le client (unicité OK)")
    void shouldUpdateClient() throws ImtException {
        // Given
        String clientId = UUID.randomUUID().toString();
        Client updateClient = Client.builder()
                .id(clientId)
                .lastName("Updated")
                .firstName("Name")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .licenseNumber("010203040506") // Format valide 12 chiffres
                .address("New Adress")
                .build();

        // Mock: Unicité OK (soit empty, soit le même client trouvé)
        // Cas 1 : Le repository ne trouve personne avec ce permis (parfait)
        when(repository.findByLicenseNumber(updateClient.getLicenseNumber()))
                .thenReturn(Optional.empty());
        // Cas 2 : Le repository ne trouve personne avec ce nom (parfait)
        when(repository.findByLastNameAndFirstNameAndBirthDate(any(), any(), any()))
                .thenReturn(Optional.empty());

        // When
        service.update(updateClient);

        // Then
        verify(repository).save(updateClient);
    }

    @Test
    @DisplayName("UPDATE - Doit échouer si le nouveau permis appartient à un AUTRE client")
    void shouldFailUpdateIfLicenseTakenByOther() {
        // Given
        String myId = "my-id";
        String otherId = "other-id";
        String conflictLicense = "999999999999";

        Client clientToUpdate = Client.builder()
                .id(myId)
                .lastName("Me")
                .firstName("Me")
                .dateOfBirth(LocalDate.EPOCH)
                .licenseNumber(conflictLicense)
                .address("Address")
                .build();

        // Mock : On trouve un client en base avec ce permis, mais c'est un AUTRE (otherId)
        Client otherClient = Client.builder().id(otherId).build();

        when(repository.findByLicenseNumber(conflictLicense))
                .thenReturn(Optional.of(otherClient));

        // When & Then
        assertThatThrownBy(() -> service.update(clientToUpdate))
                .isInstanceOf(ConflictException.class);

        verify(repository, never()).save(any());
    }
}