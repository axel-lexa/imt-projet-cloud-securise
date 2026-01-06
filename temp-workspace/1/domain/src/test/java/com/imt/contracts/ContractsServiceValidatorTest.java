package com.imt.contracts;

import com.imt.contracts.model.Contract;
import com.imt.contracts.model.ContractStateEnum;
import com.imt.common.exceptions.BadRequestException;
import com.imt.common.exceptions.ConflictException;
import com.imt.common.exceptions.ImtException;
import com.imt.vehicle.model.VehicleStateEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContractsServiceValidator - Tests d'intégration du domaine")
class ContractsServiceValidatorTest {

    @Mock
    private ContractStorageProvider repository;

    private ContractsServiceValidator service;

    private UUID contractId;
    private UUID clientId;
    private UUID vehicleId;

    @BeforeEach
    void setUp() {
        // On injecte le mock du repository dans le service
        service = new ContractsServiceValidator(repository);

        contractId = UUID.randomUUID();
        clientId = UUID.randomUUID();
        vehicleId = UUID.randomUUID();
    }

    @Test
    @DisplayName("CREATE - Doit créer le contrat quand tout est valide")
    void shouldCreateContractWhenAllValid() throws ImtException {
        // Given
        Contract newContract = Contract.builder()
                .identifier(contractId)
                .clientIdentifier(clientId)
                .vehicleIdentifier(vehicleId)
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2024, 1, 31))
                .state(ContractStateEnum.PENDING)
                .vehicleState(VehicleStateEnum.AVAILABLE)
                .build();

        // Mock : aucun contrat existant pour ce véhicule
        when(repository.findByVehicleIdentifier(vehicleId)).thenReturn(Collections.emptyList());
        // Mock de la sauvegarde
        when(repository.save(any(Contract.class))).thenReturn(newContract);

        // When
        Contract created = service.create(newContract);

        // Then
        assertThat(created).isNotNull();
        verify(repository).save(newContract);
    }

    @Test
    @DisplayName("CREATE - Doit échouer si l'état du contrat est UNKNOWN")
    void shouldNotCreateContractWhenStateIsUnknown() {
        // Given
        Contract invalidContract = Contract.builder()
                .clientIdentifier(clientId)
                .vehicleIdentifier(vehicleId)
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2024, 1, 31))
                .state(ContractStateEnum.UNKNOWN)
                .vehicleState(VehicleStateEnum.AVAILABLE)
                .build();

        // When & Then
        assertThatThrownBy(() -> service.create(invalidContract))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("statut communiqué ne correspond pas aux valeurs acceptables");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("CREATE - Doit échouer si la date de début est après la date de fin")
    void shouldNotCreateContractWhenStartDateAfterEndDate() {
        // Given
        Contract invalidContract = Contract.builder()
                .clientIdentifier(clientId)
                .vehicleIdentifier(vehicleId)
                .startDate(LocalDate.of(2024, 1, 31))
                .endDate(LocalDate.of(2024, 1, 1))
                .state(ContractStateEnum.PENDING)
                .vehicleState(VehicleStateEnum.AVAILABLE)
                .build();

        // When & Then
        assertThatThrownBy(() -> service.create(invalidContract))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("La date de début doit précéder ou être égale à la date de fin");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("CREATE - Doit échouer si le véhicule est en panne")
    void shouldNotCreateContractWhenVehicleIsBroken() {
        // Given
        Contract invalidContract = Contract.builder()
                .clientIdentifier(clientId)
                .vehicleIdentifier(vehicleId)
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2024, 1, 31))
                .state(ContractStateEnum.PENDING)
                .vehicleState(VehicleStateEnum.BROKEN)
                .build();

        // When & Then
        assertThatThrownBy(() -> service.create(invalidContract))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Les véhicules déclarés en panne ne peuvent pas être loués");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("CREATE - Doit échouer si l'état du véhicule est UNKNOWN")
    void shouldNotCreateContractWhenVehicleStateIsUnknown() {
        // Given
        Contract invalidContract = Contract.builder()
                .clientIdentifier(clientId)
                .vehicleIdentifier(vehicleId)
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2024, 1, 31))
                .state(ContractStateEnum.PENDING)
                .vehicleState(VehicleStateEnum.UNKNOWN)
                .build();

        // When & Then
        assertThatThrownBy(() -> service.create(invalidContract))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("L'état du véhicule doit être défini");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("CREATE - Doit échouer si le véhicule est déjà réservé sur la période")
    void shouldNotCreateContractWhenVehicleIsAlreadyRented() {
        // Given
        Contract newContract = Contract.builder()
                .identifier(contractId)
                .clientIdentifier(clientId)
                .vehicleIdentifier(vehicleId)
                .startDate(LocalDate.of(2024, 2, 1))
                .endDate(LocalDate.of(2024, 2, 28))
                .state(ContractStateEnum.PENDING)
                .vehicleState(VehicleStateEnum.AVAILABLE)
                .build();

        // Un contrat existant qui bloque le véhicule sur une période qui chevauche
        UUID existingContractId = UUID.randomUUID();
        Contract existingContract = Contract.builder()
                .identifier(existingContractId)
                .clientIdentifier(UUID.randomUUID())
                .vehicleIdentifier(vehicleId)
                .startDate(LocalDate.of(2024, 1, 15))
                .endDate(LocalDate.of(2024, 2, 15))
                .state(ContractStateEnum.IN_PROGRESS) // État qui bloque le véhicule
                .vehicleState(VehicleStateEnum.IN_RENTAL)
                .build();

        // Mock : on trouve un contrat existant qui bloque
        when(repository.findByVehicleIdentifier(vehicleId))
                .thenReturn(Collections.singletonList(existingContract));

        // When & Then
        assertThatThrownBy(() -> service.create(newContract))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Le véhicule est déjà réservé");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("CREATE - Doit réussir si le véhicule a un contrat terminé qui ne bloque pas")
    void shouldCreateContractWhenVehicleHasNonBlockingContract() throws ImtException {
        // Given
        Contract newContract = Contract.builder()
                .identifier(contractId)
                .clientIdentifier(clientId)
                .vehicleIdentifier(vehicleId)
                .startDate(LocalDate.of(2024, 2, 1))
                .endDate(LocalDate.of(2024, 2, 28))
                .state(ContractStateEnum.PENDING)
                .vehicleState(VehicleStateEnum.AVAILABLE)
                .build();

        // Un contrat existant terminé qui ne bloque pas le véhicule
        UUID existingContractId = UUID.randomUUID();
        Contract existingContract = Contract.builder()
                .identifier(existingContractId)
                .clientIdentifier(UUID.randomUUID())
                .vehicleIdentifier(vehicleId)
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2024, 1, 31))
                .state(ContractStateEnum.COMPLETED) // État qui ne bloque pas
                .vehicleState(VehicleStateEnum.AVAILABLE)
                .build();

        // Mock : on trouve un contrat existant mais terminé
        when(repository.findByVehicleIdentifier(vehicleId))
                .thenReturn(Collections.singletonList(existingContract));
        when(repository.save(any(Contract.class))).thenReturn(newContract);

        // When
        Contract created = service.create(newContract);

        // Then
        assertThat(created).isNotNull();
        verify(repository).save(newContract);
    }

    @Test
    @DisplayName("UPDATE - Doit mettre à jour le contrat (validation OK)")
    void shouldUpdateContract() throws ImtException {
        // Given
        Contract updateContract = Contract.builder()
                .identifier(contractId)
                .clientIdentifier(clientId)
                .vehicleIdentifier(vehicleId)
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2024, 1, 31))
                .state(ContractStateEnum.IN_PROGRESS)
                .vehicleState(VehicleStateEnum.IN_RENTAL)
                .build();

        // Mock: Aucun autre contrat qui bloque
        when(repository.findByVehicleIdentifier(vehicleId)).thenReturn(Collections.emptyList());

        // When
        service.update(updateContract);

        // Then
        verify(repository).save(updateContract);
    }

    @Test
    @DisplayName("UPDATE - Doit échouer si un autre contrat bloque le véhicule sur la période")
    void shouldFailUpdateIfOtherContractBlocksVehicle() {
        // Given
        Contract contractToUpdate = Contract.builder()
                .identifier(contractId)
                .clientIdentifier(clientId)
                .vehicleIdentifier(vehicleId)
                .startDate(LocalDate.of(2024, 2, 1))
                .endDate(LocalDate.of(2024, 2, 28))
                .state(ContractStateEnum.IN_PROGRESS)
                .vehicleState(VehicleStateEnum.IN_RENTAL)
                .build();

        // Un AUTRE contrat qui bloque le véhicule
        UUID otherContractId = UUID.randomUUID();
        Contract otherContract = Contract.builder()
                .identifier(otherContractId)
                .clientIdentifier(UUID.randomUUID())
                .vehicleIdentifier(vehicleId)
                .startDate(LocalDate.of(2024, 1, 15))
                .endDate(LocalDate.of(2024, 2, 15))
                .state(ContractStateEnum.IN_PROGRESS)
                .vehicleState(VehicleStateEnum.IN_RENTAL)
                .build();

        when(repository.findByVehicleIdentifier(vehicleId))
                .thenReturn(Collections.singletonList(otherContract));

        // When & Then
        assertThatThrownBy(() -> service.update(contractToUpdate))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Le véhicule est déjà réservé");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("UPDATE - Doit réussir si le même contrat bloque (mise à jour de soi-même)")
    void shouldSucceedUpdateWhenSameContractBlocks() throws ImtException {
        // Given
        Contract contractToUpdate = Contract.builder()
                .identifier(contractId)
                .clientIdentifier(clientId)
                .vehicleIdentifier(vehicleId)
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2024, 1, 31))
                .state(ContractStateEnum.IN_PROGRESS)
                .vehicleState(VehicleStateEnum.IN_RENTAL)
                .build();

        // Le même contrat existe déjà (mise à jour)
        when(repository.findByVehicleIdentifier(vehicleId))
                .thenReturn(Collections.singletonList(contractToUpdate));
        when(repository.save(any(Contract.class))).thenReturn(contractToUpdate);

        // When
        service.update(contractToUpdate);

        // Then
        verify(repository).save(contractToUpdate);
    }
}

