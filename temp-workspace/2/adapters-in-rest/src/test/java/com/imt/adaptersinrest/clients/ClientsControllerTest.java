package com.imt.adaptersinrest.clients;

import com.imt.adaptersinrest.clients.model.input.ClientInput;
import com.imt.adaptersinrest.clients.model.input.ClientUpdateInput;
import com.imt.adaptersinrest.clients.model.output.ClientOutput;
import com.imt.clients.ClientsService;
import com.imt.clients.ClientsServiceValidator;
import com.imt.clients.model.Client;
import com.imt.common.exceptions.ConflictException;
import com.imt.common.exceptions.ImtException;
import com.imt.common.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClientsController - Tests unitaires")
class ClientsControllerTest {

    @Mock
    private ClientsServiceValidator clientsServiceValidator;

    @Mock
    private ClientsService clientsService;

    private ClientsController clientsController;

    private ClientInput clientInput;
    private Client clientDomain;
    private final String clientId = "client-123";

    @BeforeEach
    void setUp() {
        // Instanciation manuelle du contrôleur
        clientsController = new ClientsController(clientsServiceValidator, clientsService);

        // Données de test
        clientInput = new ClientInput();
        clientInput.setLastName("Dupont");
        clientInput.setFirstName("Jean");
        clientInput.setDateOfBirth(LocalDate.of(1990, 1, 1));
        clientInput.setLicenseNumber("AB123CD");
        clientInput.setAddress("Paris");

        clientDomain = Client.builder()
                .id(clientId)
                .lastName("Dupont")
                .firstName("Jean")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .licenseNumber("AB123CD")
                .address("Paris")
                .build();
    }

    @Nested
    @DisplayName("createClient (POST)")
    class CreateTests {

        @Test
        @DisplayName("Doit créer un client avec succès (201 Created)")
        void shouldCreateClientSuccessfully() throws ImtException {
            // Given
            // On mocke le retour du service. Le controller va convertir l'input en domain lui-même.
            when(clientsServiceValidator.create(any(Client.class))).thenReturn(clientDomain);

            // When
            ResponseEntity<ClientOutput> response = clientsController.create(clientInput);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();

            // Vérification que la conversion de sortie s'est bien passée
            ClientOutput output = response.getBody();
            assertThat(output.getId()).isEqualTo(clientId);
            assertThat(output.getLastName()).isEqualTo("Dupont");

            // Vérifie que le service a été appelé
            verify(clientsServiceValidator).create(any(Client.class));
        }

        @Test
        @DisplayName("Doit propager l'exception si le service échoue")
        void shouldPropagateExceptionWhenServiceFails() throws ImtException {
            // Given
            doThrow(new ConflictException("Client existe déjà"))
                    .when(clientsServiceValidator).create(any(Client.class));

            // When & Then
            assertThatThrownBy(() -> clientsController.create(clientInput))
                    .isInstanceOf(ConflictException.class)
                    .hasMessage("Client existe déjà");
        }
    }

    @Nested
    @DisplayName("updateClient (PATCH)")
    class UpdateTests {

        private ClientUpdateInput updateInput;

        @BeforeEach
        void setUpUpdate() {
            updateInput = new ClientUpdateInput();
            // Simulation d'une mise à jour : on change le nom
            // Comme vous n'avez pas de méthode factory statique facile, on peut mocker UpdatableProperty
            // ou utiliser la réflexion, mais le plus simple est de modifier votre DTO pour avoir des setters classiques
            // ou des méthodes 'makesChanges' publiques.
            // Supposons ici que les champs sont initialisés à empty() par défaut.
        }

        @Test
        @DisplayName("Doit mettre à jour un client existant avec succès (200 OK)")
        void shouldUpdateClientSuccessfully() throws ImtException {
            // Given
            when(clientsService.getOne(clientId)).thenReturn(Optional.of(clientDomain));
            when(clientsServiceValidator.update(any(Client.class))).thenReturn(clientDomain);

            // When
            ResponseEntity<ClientOutput> response = clientsController.update(clientId, updateInput);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getId()).isEqualTo(clientId);

            verify(clientsServiceValidator).update(any(Client.class));
        }

        @Test
        @DisplayName("Doit lever une exception si le client n'existe pas")
        void shouldThrowExceptionWhenClientNotFound() throws ImtException {
            // Given
            when(clientsService.getOne(clientId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> clientsController.update(clientId, updateInput))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Client non trouvé");

            verify(clientsServiceValidator, never()).update(any(Client.class));
        }
    }

    @Nested
    @DisplayName("getClientById (GET)")
    class GetByIdTests {

        @Test
        @DisplayName("Doit retourner un client existant (200 OK)")
        void shouldReturnClientWhenFound() throws ImtException {
            // Given
            when(clientsService.getOne(clientId)).thenReturn(Optional.of(clientDomain));

            // When
            ResponseEntity<ClientOutput> response = clientsController.getById(clientId);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getId()).isEqualTo(clientId);
        }

        @Test
        @DisplayName("Doit lever une exception si le client n'est pas trouvé")
        void shouldThrowExceptionWhenNotFound() {
            // Given
            when(clientsService.getOne(clientId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> clientsController.getById(clientId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getAllClients (GET)")
    class GetAllTests {

        @Test
        @DisplayName("Doit retourner la liste des clients")
        void shouldReturnAllClients() {
            // Given
            List<Client> clients = Arrays.asList(clientDomain, clientDomain);
            when(clientsService.getAll()).thenReturn(clients);

            // When
            ResponseEntity<Collection<ClientOutput>> response = clientsController.getAll();

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).hasSize(2);
            verify(clientsService).getAll();
        }
    }

    @Nested
    @DisplayName("deleteClient (DELETE)")
    class DeleteTests {

        @Test
        @DisplayName("Doit supprimer un client avec succès (204 No Content)")
        void shouldDeleteClientSuccessfully() throws ImtException {
            // When
            ResponseEntity<Void> response = clientsController.delete(clientId);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            verify(clientsService).delete(clientId);
        }
    }
}