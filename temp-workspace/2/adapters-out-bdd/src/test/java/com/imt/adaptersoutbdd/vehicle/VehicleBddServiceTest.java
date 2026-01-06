package com.imt.adaptersoutbdd.vehicle;

import com.imt.adaptersoutbdd.vehicle.repositories.VehicleRepository;
import com.imt.adaptersoutbdd.vehicle.repositories.entities.VehicleEntity;
import com.imt.adaptersoutbdd.vehicle.repositories.mappers.VehicleBddMapper;
import com.imt.vehicle.model.EngineTypeEnum;
import com.imt.vehicle.model.Vehicle;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VehicleBddService - Tests unitaires")
class VehicleBddServiceTest {

    @Mock
    private VehicleRepository repository;

    @Mock
    private VehicleBddMapper mapper;

    @InjectMocks
    private VehicleBddService service;

    private Vehicle vehicle;
    private VehicleEntity vehicleEntity;
    private final String vehicleId = "veh-123";
    private final String licensePlate = "AA-123-BB";

    @BeforeEach
    void setUp() {
        LocalDate acquisitionDate = LocalDate.of(2023, 1, 1);

        vehicle = Vehicle.builder()
                .id(vehicleId)
                .licensePlate(licensePlate)
                .brand("Renault")
                .model("Clio")
                .engineType(EngineTypeEnum.GASOLINE)
                .color("Blue")
                .acquisitionDate(acquisitionDate)
                .state(VehicleStateEnum.AVAILABLE)
                .build();

        vehicleEntity = VehicleEntity.builder()
                .id(vehicleId)
                .licensePlate(licensePlate)
                .brand("Renault")
                .model("Clio")
                .engineType(EngineTypeEnum.GASOLINE)
                .color("Blue")
                .acquisitionDate(acquisitionDate)
                .state(VehicleStateEnum.AVAILABLE)
                .build();
    }

    @Test
    @DisplayName("getAll() - Doit retourner tous les véhicules mappés")
    void getAll_shouldReturnAllMappedVehicles() {
        // Given
        when(repository.findAll()).thenReturn(List.of(vehicleEntity));
        when(mapper.from(vehicleEntity)).thenReturn(vehicle);

        // When
        Collection<Vehicle> result = service.getAll();

        // Then
        assertThat(result).isNotNull().hasSize(1).containsExactly(vehicle);
        verify(repository).findAll();
        verify(mapper).from(vehicleEntity);
    }

    @Test
    @DisplayName("getAll() - Doit retourner une liste vide si aucun véhicule n'est trouvé")
    void getAll_shouldReturnEmptyList_whenNoVehicles() {
        // Given
        when(repository.findAll()).thenReturn(Collections.emptyList());

        // When
        Collection<Vehicle> result = service.getAll();

        // Then
        assertThat(result).isNotNull().isEmpty();
        verify(repository).findAll();
        verifyNoInteractions(mapper);
    }

    @Test
    @DisplayName("get() - Doit retourner le véhicule mappé si trouvé")
    void get_shouldReturnVehicle_whenFound() {
        // Given
        when(repository.findById(vehicleId)).thenReturn(Optional.of(vehicleEntity));
        when(mapper.from(vehicleEntity)).thenReturn(vehicle);

        // When
        Optional<Vehicle> result = service.get(vehicleId);

        // Then
        assertThat(result).isPresent().contains(vehicle);
        verify(repository).findById(vehicleId);
        verify(mapper).from(vehicleEntity);
    }

    @Test
    @DisplayName("get() - Doit retourner un Optional vide si non trouvé")
    void get_shouldReturnEmpty_whenNotFound() {
        // Given
        when(repository.findById(vehicleId)).thenReturn(Optional.empty());

        // When
        Optional<Vehicle> result = service.get(vehicleId);

        // Then
        assertThat(result).isEmpty();
        verify(repository).findById(vehicleId);
        verifyNoInteractions(mapper);
    }

    @Test
    @DisplayName("getByLicensePlate() - Doit retourner le véhicule mappé si trouvé")
    void getByLicensePlate_shouldReturnVehicle_whenFound() {
        // Given
        when(repository.findByLicensePlate(licensePlate)).thenReturn(Optional.of(vehicleEntity));
        when(mapper.from(vehicleEntity)).thenReturn(vehicle);

        // When
        Optional<Vehicle> result = service.getByLicensePlate(licensePlate);

        // Then
        assertThat(result).isPresent().contains(vehicle);
        verify(repository).findByLicensePlate(licensePlate);
        verify(mapper).from(vehicleEntity);
    }

    @Test
    @DisplayName("getByLicensePlate() - Doit retourner un Optional vide si non trouvé")
    void getByLicensePlate_shouldReturnEmpty_whenNotFound() {
        // Given
        when(repository.findByLicensePlate(licensePlate)).thenReturn(Optional.empty());

        // When
        Optional<Vehicle> result = service.getByLicensePlate(licensePlate);

        // Then
        assertThat(result).isEmpty();
        verify(repository).findByLicensePlate(licensePlate);
        verifyNoInteractions(mapper);
    }

    @Test
    @DisplayName("save() - Doit mapper, sauvegarder et retourner le véhicule mappé")
    void save_shouldSaveAndReturnVehicle() {
        // Given
        when(mapper.to(vehicle)).thenReturn(vehicleEntity);
        when(repository.save(vehicleEntity)).thenReturn(vehicleEntity);
        when(mapper.from(vehicleEntity)).thenReturn(vehicle);

        // When
        Vehicle result = service.save(vehicle);

        // Then
        assertThat(result).isNotNull().isEqualTo(vehicle);
        verify(mapper).to(vehicle);
        verify(repository).save(vehicleEntity);
        verify(mapper).from(vehicleEntity);
    }

    @Test
    @DisplayName("delete() - Doit appeler la méthode delete du repository")
    void delete_shouldCallRepositoryDelete() {
        // When
        service.delete(vehicleId);

        // Then
        verify(repository).deleteById(vehicleId);
    }

    @Test
    @DisplayName("exist() - Doit appeler la méthode existsById du repository")
    void exist_shouldCallRepositoryExists() {
        // Given
        when(repository.existsById(vehicleId)).thenReturn(true);

        // When
        boolean result = service.exist(vehicleId);

        // Then
        assertThat(result).isTrue();
        verify(repository).existsById(vehicleId);
    }
}
