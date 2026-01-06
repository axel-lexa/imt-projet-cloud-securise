package com.imt.vehicle;

import com.imt.common.exceptions.ImtException;
import com.imt.vehicle.model.EngineTypeEnum;
import com.imt.vehicle.model.Vehicle;
import com.imt.vehicle.model.VehicleStateEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleStorageProvider service;

    @InjectMocks
    private VehicleService vehicleService;

    private Vehicle vehicle;

    @BeforeEach
    void setUp() {
        vehicle = Vehicle.builder()
                .id("123")
                .brand("Renault")
                .model("Clio")
                .engineType(EngineTypeEnum.GASOLINE)
                .color("Bleu")
                .licensePlate("AA-123-BB")
                .acquisitionDate(LocalDate.now())
                .state(VehicleStateEnum.AVAILABLE)
                .build();
    }

    @Test
    void getAll_shouldReturnVehicles_whenProviderReturnsVehicles() {
        // Given
        when(service.getAll()).thenReturn(Set.of(vehicle));

        // When
        var result = vehicleService.getAll();

        // Then
        assertThat(result).containsExactly(vehicle);
        verify(service).getAll();
    }

    @Test
    void getAll_shouldReturnEmptySet_whenProviderReturnsNull() {
        // Given
        when(service.getAll()).thenReturn(null);

        // When
        var result = vehicleService.getAll();

        // Then
        assertThat(result).isEmpty();
        verify(service).getAll();
    }

    @Test
    void getOne_shouldReturnVehicle_whenFound() {
        // Given
        when(service.get("123")).thenReturn(Optional.of(vehicle));

        // When
        var result = vehicleService.getOne("123");

        // Then
        assertThat(result).isPresent().contains(vehicle);
        verify(service).get("123");
    }

    @Test
    void getOne_shouldReturnEmpty_whenNotFound() {
        // Given
        when(service.get("456")).thenReturn(Optional.empty());

        // When
        var result = vehicleService.getOne("456");

        // Then
        assertThat(result).isNotPresent();
        verify(service).get("456");
    }

    @Test
    void getByLicensePlate_shouldReturnVehicle_whenFound() {
        // Given
        when(service.getByLicensePlate("AA-123-BB")).thenReturn(Optional.of(vehicle));

        // When
        var result = vehicleService.getByLicensePlate("AA-123-BB");

        // Then
        assertThat(result).isPresent().contains(vehicle);
        verify(service).getByLicensePlate("AA-123-BB");
    }

    @Test
    void getByLicensePlate_shouldReturnEmpty_whenNotFound() {
        // Given
        when(service.getByLicensePlate("ZZ-999-ZZ")).thenReturn(Optional.empty());

        // When
        var result = vehicleService.getByLicensePlate("ZZ-999-ZZ");

        // Then
        assertThat(result).isNotPresent();
        verify(service).getByLicensePlate("ZZ-999-ZZ");
    }

    @Test
    void create_shouldCallProviderAndReturnVehicle() throws ImtException {
        // Given
        when(service.save(vehicle)).thenReturn(vehicle);

        // When
        var result = vehicleService.create(vehicle);

        // Then
        assertThat(result).isEqualTo(vehicle);
        verify(service).save(vehicle);
    }

    @Test
    void update_shouldCallProvider() throws ImtException {
        // When
        vehicleService.update(vehicle);

        // Then
        verify(service).save(vehicle);
    }

    @Test
    void delete_shouldCallProvider() throws ImtException {
        // When
        vehicleService.delete("123");

        // Then
        verify(service).delete("123");
    }
}
