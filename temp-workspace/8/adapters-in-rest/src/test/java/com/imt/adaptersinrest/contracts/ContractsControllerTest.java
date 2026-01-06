package com.imt.adaptersinrest.contracts;

import com.imt.adaptersinrest.contracts.model.input.ContractInput;
import com.imt.adaptersinrest.contracts.model.input.ContractUpdateInput;
import com.imt.adaptersinrest.contracts.model.output.ContractOutput;
import com.imt.common.exceptions.ImtException;
import com.imt.contracts.ContractsServiceValidator;
import com.imt.contracts.model.Contract;
import com.imt.contracts.model.ContractStateEnum;
import com.imt.vehicle.model.VehicleStateEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.imt.common.exceptions.BadRequestException;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContractsController - Tests unitaires")
class ContractsControllerTest {

    @Mock
    private ContractsServiceValidator contractsServiceValidator;

    private ContractsController controller;

    private ContractInput contractInput;
    private Contract contractDomain;
    private UUID contractId;
    private UUID clientId;
    private UUID vehicleId;

    @BeforeEach
    void setUp() {
        // Instanciation manuelle du contrôleur
        controller = new ContractsController(contractsServiceValidator);

        // Données de test
        contractId = UUID.randomUUID();
        clientId = UUID.randomUUID();
        vehicleId = UUID.randomUUID();

        contractInput = new ContractInput();
        contractInput.setClientIdentifier(clientId);
        contractInput.setVehicleIdentifier(vehicleId);
        contractInput.setStartDate(LocalDate.of(2024, 1, 1));
        contractInput.setEndDate(LocalDate.of(2024, 1, 31));
        contractInput.setState(ContractStateEnum.PENDING);
        contractInput.setVehicleState(VehicleStateEnum.AVAILABLE);

        contractDomain = Contract.builder()
                .identifier(contractId)
                .clientIdentifier(clientId)
                .vehicleIdentifier(vehicleId)
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2024, 1, 31))
                .state(ContractStateEnum.PENDING)
                .vehicleState(VehicleStateEnum.AVAILABLE)
                .build();
    }

    @Nested
    @DisplayName("create (POST)")
    class CreateTests {

        @Test
        @DisplayName("Doit créer un contrat avec succès (201 Created)")
        void shouldCreateContractSuccessfully() throws ImtException {
            // Given
            when(contractsServiceValidator.create(any(Contract.class))).thenReturn(contractDomain);

            // When
            ContractOutput response = controller.create(contractInput);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getIdentifier()).isEqualTo(contractId);
            assertThat(response.getClientIdentifier()).isEqualTo(clientId);
            assertThat(response.getVehicleIdentifier()).isEqualTo(vehicleId);
            assertThat(response.getState()).isEqualTo(ContractStateEnum.PENDING);

            // Vérifie que le service a été appelé
            verify(contractsServiceValidator).create(any(Contract.class));
        }

        @Test
        @DisplayName("Doit propager l'exception si le service échoue")
        void shouldPropagateExceptionWhenServiceFails() throws ImtException {
            // Given
            doThrow(new BadRequestException("Erreur de validation"))
                    .when(contractsServiceValidator).create(any(Contract.class));

            // When & Then
            assertThatThrownBy(() -> controller.create(contractInput))
                    .isInstanceOf(ImtException.class)
                    .hasMessage("Erreur de validation");
        }
    }

    @Nested
    @DisplayName("update (PATCH)")
    class UpdateTests {

        private ContractUpdateInput updateInput;

        @BeforeEach
        void setUpUpdate() {
            updateInput = new ContractUpdateInput();
            // Les champs sont initialisés à empty() par défaut
            // Pour les tests, on peut utiliser la réflexion ou simplement tester avec des valeurs vides
        }

        @Test
        @DisplayName("Doit mettre à jour un contrat existant avec succès (204 No Content)")
        void shouldUpdateContractSuccessfully() throws ImtException {
            // Given
            when(contractsServiceValidator.getOne(contractId)).thenReturn(Optional.of(contractDomain));
            doNothing().when(contractsServiceValidator).update(any(Contract.class));

            // When
            controller.update(contractId.toString(), updateInput);

            // Then
            verify(contractsServiceValidator).getOne(contractId);
            verify(contractsServiceValidator).update(any(Contract.class));
        }

        @Test
        @DisplayName("Doit lever une exception si le contrat n'existe pas")
        void shouldThrowExceptionWhenContractNotFound() throws ImtException {
            // Given
            when(contractsServiceValidator.getOne(contractId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> controller.update(contractId.toString(), updateInput))
                    .isInstanceOf(NoSuchElementException.class)
                    .hasMessageContaining("Contrat non trouvé.");

            verify(contractsServiceValidator, never()).update(any(Contract.class));
        }
    }

    @Nested
    @DisplayName("getOne (GET)")
    class GetByIdTests {

        @Test
        @DisplayName("Doit retourner un contrat existant (200 OK)")
        void shouldReturnContractWhenFound() {
            // Given
            when(contractsServiceValidator.getOne(contractId)).thenReturn(Optional.of(contractDomain));

            // When
            ContractOutput response = controller.getOne(contractId.toString());

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getIdentifier()).isEqualTo(contractId);
            assertThat(response.getClientIdentifier()).isEqualTo(clientId);
        }

        @Test
        @DisplayName("Doit lever une exception si le contrat n'est pas trouvé")
        void shouldThrowExceptionWhenNotFound() {
            // Given
            when(contractsServiceValidator.getOne(contractId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> controller.getOne(contractId.toString()))
                    .isInstanceOf(NoSuchElementException.class)
                    .hasMessageContaining("Contrat non trouvé.");
        }
    }

    @Nested
    @DisplayName("getAll (GET)")
    class GetAllTests {

        @Test
        @DisplayName("Doit retourner la liste des contrats")
        void shouldReturnAllContracts() {
            // Given
            List<Contract> contracts = Arrays.asList(contractDomain, contractDomain);
            when(contractsServiceValidator.getAll()).thenReturn(contracts);

            // When
            Collection<ContractOutput> response = controller.getAll();

            // Then
            assertThat(response).isNotNull();
            assertThat(response).hasSize(2);
            verify(contractsServiceValidator).getAll();
        }

        @Test
        @DisplayName("Doit retourner une liste vide si aucun contrat")
        void shouldReturnEmptyListWhenNoContracts() {
            // Given
            when(contractsServiceValidator.getAll()).thenReturn(Collections.emptyList());

            // When
            Collection<ContractOutput> response = controller.getAll();

            // Then
            assertThat(response).isNotNull();
            assertThat(response).isEmpty();
        }
    }

    @Nested
    @DisplayName("getByClient (GET)")
    class GetByClientTests {

        @Test
        @DisplayName("Doit retourner les contrats d'un client")
        void shouldReturnContractsByClient() {
            // Given
            List<Contract> contracts = Arrays.asList(contractDomain);
            when(contractsServiceValidator.getByClient(clientId)).thenReturn(contracts);

            // When
            Collection<ContractOutput> response = controller.getByClientId(clientId.toString());

            // Then
            assertThat(response).isNotNull();
            assertThat(response).hasSize(1);
            verify(contractsServiceValidator).getByClient(clientId);
        }
    }

    @Nested
    @DisplayName("getByVehicle (GET)")
    class GetByVehicleTests {

        @Test
        @DisplayName("Doit retourner les contrats d'un véhicule")
        void shouldReturnContractsByVehicle() {
            // Given
            List<Contract> contracts = Arrays.asList(contractDomain);
            when(contractsServiceValidator.getByVehicle(vehicleId)).thenReturn(contracts);

            // When
            Collection<ContractOutput> response = controller.getByVehicleId(vehicleId.toString());

            // Then
            assertThat(response).isNotNull();
            assertThat(response).hasSize(1);
            verify(contractsServiceValidator).getByVehicle(vehicleId);
        }
    }

    @Nested
    @DisplayName("delete (DELETE)")
    class DeleteTests {

        @Test
        @DisplayName("Doit supprimer un contrat avec succès (204 No Content)")
        void shouldDeleteContractSuccessfully() throws ImtException {
            // When
            controller.delete(contractId.toString());

            // Then
            verify(contractsServiceValidator).delete(contractId);
        }
    }
}

