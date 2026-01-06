package com.imt.adaptersoutbdd.contracts;

import com.imt.adaptersoutbdd.contracts.repositories.ContractRepository;
import com.imt.adaptersoutbdd.contracts.repositories.entities.ContractEntity;
import com.imt.adaptersoutbdd.contracts.repositories.mappers.ContractBddMapper;
import com.imt.contracts.model.Contract;
import com.imt.contracts.model.ContractStateEnum;
import com.imt.vehicle.model.VehicleStateEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContractsBddService - Tests unitaires")
class ContractsBddServiceTest {

    @Mock
    private ContractRepository repository;

    @Mock
    private ContractBddMapper mapper;

    @InjectMocks
    private ContractsBddService service;

    private UUID testId;
    private String testIdString;
    private UUID clientId;
    private String clientIdString;
    private UUID vehicleId;
    private String vehicleIdString;
    private Contract testContract;
    private ContractEntity testContractEntity;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        testIdString = testId.toString();
        clientId = UUID.randomUUID();
        clientIdString = clientId.toString();
        vehicleId = UUID.randomUUID();
        vehicleIdString = vehicleId.toString();

        testContract = Contract.builder()
                .identifier(testId)
                .clientIdentifier(clientId)
                .vehicleIdentifier(vehicleId)
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2024, 1, 31))
                .state(ContractStateEnum.PENDING)
                .vehicleState(VehicleStateEnum.AVAILABLE)
                .build();

        testContractEntity = ContractEntity.builder()
                .id(testIdString)
                .clientId(clientIdString)
                .vehicleId(vehicleIdString)
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2024, 1, 31))
                .state(ContractStateEnum.PENDING)
                .vehicleState(VehicleStateEnum.AVAILABLE)
                .build();
    }

    @Test
    @DisplayName("exist() - Vérifie l'appel au repository")
    void exist_shouldReturnTrue_whenContractExists() {
        // Given
        when(repository.existsById(testIdString)).thenReturn(true);

        // When
        boolean result = service.exist(testId);

        // Then
        assertTrue(result);
        verify(repository).existsById(testIdString);
    }

    @Test
    @DisplayName("exist() - Retourne false si le contrat n'existe pas")
    void exist_shouldReturnFalse_whenContractDoesNotExist() {
        // Given
        when(repository.existsById(testIdString)).thenReturn(false);

        // When
        boolean result = service.exist(testId);

        // Then
        assertFalse(result);
        verify(repository).existsById(testIdString);
    }

    @Test
    @DisplayName("getAll() - Retourne la liste mappée")
    void getAll_shouldReturnAllContracts() {
        // Given
        when(repository.findAll()).thenReturn(List.of(testContractEntity));
        when(mapper.from(testContractEntity)).thenReturn(testContract);

        // When
        Collection<Contract> result = service.getAll();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains(testContract));
        verify(repository).findAll();
        verify(mapper).from(testContractEntity);
    }

    @Test
    @DisplayName("get() - Retourne le contrat mappé si trouvé")
    void get_shouldReturnContract_whenFound() {
        // Given
        when(repository.findById(testIdString)).thenReturn(Optional.of(testContractEntity));
        when(mapper.from(testContractEntity)).thenReturn(testContract);

        // When
        Optional<Contract> result = service.get(testId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testContract, result.get());
        verify(repository).findById(testIdString);
        verify(mapper).from(testContractEntity);
    }

    @Test
    @DisplayName("get() - Retourne empty si le contrat n'est pas trouvé")
    void get_shouldReturnEmpty_whenNotFound() {
        // Given
        when(repository.findById(testIdString)).thenReturn(Optional.empty());

        // When
        Optional<Contract> result = service.get(testId);

        // Then
        assertFalse(result.isPresent());
        verify(repository).findById(testIdString);
        verify(mapper, never()).from(any());
    }

    @Test
    @DisplayName("save() - Sauvegarde l'entité et retourne le contrat")
    void save_shouldSaveAndReturnContract() {
        // Given
        when(mapper.to(testContract)).thenReturn(testContractEntity);
        when(repository.save(testContractEntity)).thenReturn(testContractEntity);
        when(mapper.from(testContractEntity)).thenReturn(testContract);

        // When
        Contract result = service.save(testContract);

        // Then
        assertNotNull(result);
        assertEquals(testContract, result);
        verify(mapper).to(testContract);
        verify(repository).save(testContractEntity);
        verify(mapper).from(testContractEntity);
    }

    @Test
    @DisplayName("delete() - Appelle le delete du repository")
    void delete_shouldCallRepository() {
        // When
        service.delete(testId);

        // Then
        verify(repository).deleteById(testIdString);
    }

    @Test
    @DisplayName("findByClientIdentifier() - Appelle la méthode spécifique du repository")
    void findByClientIdentifier_shouldCallRepo() {
        // Given
        when(repository.findByClientId(clientIdString)).thenReturn(List.of(testContractEntity));
        when(mapper.from(testContractEntity)).thenReturn(testContract);

        // When
        Collection<Contract> result = service.findByClientIdentifier(clientId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains(testContract));
        verify(repository).findByClientId(clientIdString);
        verify(mapper).from(testContractEntity);
    }

    @Test
    @DisplayName("findByVehicleIdentifier() - Appelle la méthode spécifique du repository")
    void findByVehicleIdentifier_shouldCallRepo() {
        // Given
        when(repository.findByVehicleId(vehicleIdString)).thenReturn(List.of(testContractEntity));
        when(mapper.from(testContractEntity)).thenReturn(testContract);

        // When
        Collection<Contract> result = service.findByVehicleIdentifier(vehicleId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains(testContract));
        verify(repository).findByVehicleId(vehicleIdString);
        verify(mapper).from(testContractEntity);
    }

    @Test
    @DisplayName("findByVehicleIdentifierBetween() - Appelle la méthode spécifique avec dates")
    void findByVehicleIdentifierBetween_shouldCallRepo() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);

        when(repository.findByVehicleIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                vehicleIdString, startDate, endDate))
                .thenReturn(List.of(testContractEntity));
        when(mapper.from(testContractEntity)).thenReturn(testContract);

        // When
        Collection<Contract> result = service.findByVehicleIdentifierBetween(vehicleId, startDate, endDate);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains(testContract));
        verify(repository).findByVehicleIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                vehicleIdString, startDate, endDate);
        verify(mapper).from(testContractEntity);
    }

    @Test
    @DisplayName("findByVehicleIdentifierBetween() - Retourne liste vide si paramètres null")
    void findByVehicleIdentifierBetween_shouldReturnEmpty_whenNullParameters() {
        // When
        Collection<Contract> result1 = service.findByVehicleIdentifierBetween(null, LocalDate.now(), LocalDate.now());
        Collection<Contract> result2 = service.findByVehicleIdentifierBetween(vehicleId, null, LocalDate.now());
        Collection<Contract> result3 = service.findByVehicleIdentifierBetween(vehicleId, LocalDate.now(), null);

        // Then
        assertNotNull(result1);
        assertTrue(result1.isEmpty());
        assertNotNull(result2);
        assertTrue(result2.isEmpty());
        assertNotNull(result3);
        assertTrue(result3.isEmpty());

        verify(repository, never()).findByVehicleIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                any(), any(), any());
    }
}

