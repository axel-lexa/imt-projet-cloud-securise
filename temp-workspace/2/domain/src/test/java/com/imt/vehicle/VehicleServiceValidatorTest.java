package com.imt.vehicle;

import com.imt.common.exceptions.BadRequestException;
import com.imt.common.exceptions.ConflictException;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceValidatorTest {

    @Mock
    private VehicleStorageProvider service;

    @InjectMocks
    private VehicleServiceValidator vehicleServiceValidator;

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
    void create_shouldCallProvider_whenVehicleIsValid() throws ImtException {
        // Given
        when(service.getByLicensePlate(vehicle.getLicensePlate())).thenReturn(Optional.empty());
        when(service.save(vehicle)).thenReturn(vehicle);

        // When
        Vehicle result = vehicleServiceValidator.create(vehicle);

        // Then
        verify(service).save(vehicle);
        assertThat(result).isEqualTo(vehicle);
    }

    @Test
    void create_shouldThrowBadRequest_whenConstraintValidationFails() {
        // Given
        Vehicle invalidVehicle = vehicle.toBuilder().brand(null).build();

        // When & Then
        assertThatThrownBy(() -> vehicleServiceValidator.create(invalidVehicle))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("La marque ne peut pas être nulle");

        verify(service, never()).save(any());
    }

    @Test
    void create_shouldThrowConflict_whenLicensePlateAlreadyExists() {
        // Given
        Vehicle existingVehicle = vehicle.toBuilder().id("456").build();
        when(service.getByLicensePlate(vehicle.getLicensePlate())).thenReturn(Optional.of(existingVehicle));

        // When & Then
        assertThatThrownBy(() -> vehicleServiceValidator.create(vehicle))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Un véhicule avec la plaque d'immatriculation 'AA-123-BB' existe déjà.");

        verify(service, never()).save(any());
    }

    @Test
    void create_shouldThrowBadRequest_whenStateIsUnknown() {
        // Given
        Vehicle invalidVehicle = vehicle.toBuilder().state(VehicleStateEnum.UNKNOWN).build();

        // When & Then
        assertThatThrownBy(() -> vehicleServiceValidator.create(invalidVehicle))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("L'état communiqué ne correspond pas aux valeurs acceptables");

        verify(service, never()).save(any());
    }

    @Test
    void create_shouldThrowBadRequest_whenEngineTypeIsUnknown() {
        // Given
        Vehicle invalidVehicle = vehicle.toBuilder().engineType(EngineTypeEnum.UNKNOWN).build();

        // When & Then
        assertThatThrownBy(() -> vehicleServiceValidator.create(invalidVehicle))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("La motorisation communiquée ne correspond pas aux valeurs acceptables");

        verify(service, never()).save(any());
    }

    @Test
    void update_shouldCallProvider_whenVehicleIsValid() throws ImtException {
        // When
        vehicleServiceValidator.update(vehicle);

        // Then
        verify(service).save(vehicle);
    }

    @Test
    void update_shouldThrowBadRequest_whenConstraintValidationFails() {
        // Given
        Vehicle invalidVehicle = vehicle.toBuilder().model(null).build();

        // When & Then
        assertThatThrownBy(() -> vehicleServiceValidator.update(invalidVehicle))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Le modèle ne peut pas être nul");

        verify(service, never()).save(any());
    }
}
