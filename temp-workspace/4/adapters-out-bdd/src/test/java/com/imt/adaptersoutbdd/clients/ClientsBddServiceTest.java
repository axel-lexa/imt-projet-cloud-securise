package com.imt.adaptersoutbdd.clients;

import com.imt.adaptersoutbdd.clients.repositories.ClientRepository;
import com.imt.adaptersoutbdd.clients.repositories.entities.ClientEntity;
import com.imt.adaptersoutbdd.clients.repositories.mappers.ClientBddMapper;
import com.imt.clients.model.Client;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClientsBddService - Tests unitaires")
class ClientsBddServiceTest {

    @Mock
    private ClientRepository repository;

    @Mock
    private ClientBddMapper mapper;

    @InjectMocks
    private ClientsBddService service;

    private String testId = "12345";
    private Client testClient;
    private ClientEntity testClientEntity;

    @BeforeEach
    void setUp() {
        testClient = Client.builder().id(testId).lastName("Dupont").build();
        testClientEntity = ClientEntity.builder().id(testId).lastName("Dupont").build();
    }

    @Test
    @DisplayName("exist() - Vérifie l'appel au repository")
    void exist_shouldReturnTrue_whenClientExists() {
        // Given
        when(repository.existsById(testId)).thenReturn(true);
        // When
        boolean result = service.exist(testId);
        // Then
        assertTrue(result);
        verify(repository).existsById(testId);
    }

    @Test
    @DisplayName("getAll() - Retourne la liste mappée")
    void getAll_shouldReturnAllClients() {
        // Given
        when(repository.findAll()).thenReturn(List.of(testClientEntity));
        when(mapper.from(testClientEntity)).thenReturn(testClient);

        // When
        Collection<Client> result = service.getAll();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains(testClient));
        verify(repository).findAll();
    }

    @Test
    @DisplayName("get() - Retourne le client mappé si trouvé")
    void get_shouldReturnClient_whenFound() {
        // Given
        when(repository.findById(testId)).thenReturn(Optional.of(testClientEntity));
        when(mapper.from(testClientEntity)).thenReturn(testClient);

        // When
        Optional<Client> result = service.get(testId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testClient, result.get());
    }

    @Test
    @DisplayName("save() - Sauvegarde l'entité et retourne le domaine")
    void save_shouldSaveAndReturnClient() {
        // Given
        when(mapper.to(testClient)).thenReturn(testClientEntity);
        when(repository.save(testClientEntity)).thenReturn(testClientEntity);
        when(mapper.from(testClientEntity)).thenReturn(testClient);

        // When
        Client result = service.save(testClient);

        // Then
        assertEquals(testClient, result);
        verify(repository).save(testClientEntity);
    }

    @Test
    @DisplayName("delete() - Appelle le delete du repository")
    void delete_shouldCallRepository() {
        // When
        service.delete(testId);
        // Then
        verify(repository).deleteById(testId);
    }

    // --- Tests spécifiques à votre TP (License & Unicité) ---

    @Test
    @DisplayName("findByLicenseNumber() - Appelle la méthode spécifique du repository")
    void findByLicenseNumber_shouldCallRepo() {
        // Given
        String license = "PERMIS-123";
        when(repository.findByLicenseNumber(license)).thenReturn(Optional.of(testClientEntity));
        when(mapper.from(testClientEntity)).thenReturn(testClient);

        // When
        Optional<Client> result = service.findByLicenseNumber(license);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testClient, result.get());
        verify(repository).findByLicenseNumber(license);
    }

    @Test
    @DisplayName("findByLastNameAndFirstNameAndBirthDate() - Appelle la méthode spécifique")
    void findByPerson_shouldCallRepo() {
        // Given
        String nom = "Dupont";
        String prenom = "Jean";
        LocalDate date = LocalDate.now();

        when(repository.findByLastNameAndFirstNameAndDateOfBirth(nom, prenom, date))
                .thenReturn(Optional.of(testClientEntity));

        when(mapper.from(testClientEntity)).thenReturn(testClient);

        // When
        Optional<Client> result = service.findByLastNameAndFirstNameAndBirthDate(nom, prenom, date);

        // Then
        assertTrue(result.isPresent());

        verify(repository).findByLastNameAndFirstNameAndDateOfBirth(nom, prenom, date);
    }
}