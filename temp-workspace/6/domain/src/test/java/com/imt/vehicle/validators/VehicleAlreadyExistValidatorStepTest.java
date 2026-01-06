package com.imt.vehicle.validators;

import com.imt.common.exceptions.ConflictException;
import com.imt.vehicle.VehicleStorageProvider;
import com.imt.vehicle.model.Vehicle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VehicleAlreadyExistValidatorStepTest {

    @Mock
    private VehicleStorageProvider service;

    @InjectMocks
    private VehicleAlreadyExistValidatorStep validator;

    private Vehicle vehicleToValidate;

    @BeforeEach
    void setUp() {
        vehicleToValidate = Vehicle.builder()
                .id("id-1")
                .licensePlate("AA-123-BB")
                .brand("Test")
                .model("Test")
                .color("Blue")
                .acquisitionDate(LocalDate.now())
                .engineType(com.imt.vehicle.model.EngineTypeEnum.GASOLINE)
                .state(com.imt.vehicle.model.VehicleStateEnum.AVAILABLE)
                .build();
    }

    @Test
    void check_shouldDoNothing_whenLicensePlateIsUnique() {
        // Given
        when(service.getByLicensePlate("AA-123-BB")).thenReturn(Optional.empty());

        // When & Then
        assertDoesNotThrow(() -> validator.check(vehicleToValidate));
    }

    @Test
    void check_shouldDoNothing_whenVehicleIsTheSame() {
        // Given
        when(service.getByLicensePlate("AA-123-BB")).thenReturn(Optional.of(vehicleToValidate));

        // When & Then
        assertDoesNotThrow(() -> validator.check(vehicleToValidate));
    }

    @Test
    void check_shouldThrowConflictException_whenLicensePlateExistsForAnotherVehicle() {
        // Given
        Vehicle existingVehicle = vehicleToValidate.toBuilder().id("id-2").build();
        when(service.getByLicensePlate("AA-123-BB")).thenReturn(Optional.of(existingVehicle));

        // When & Then
        assertThatThrownBy(() -> validator.check(vehicleToValidate))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Un véhicule avec la plaque d'immatriculation 'AA-123-BB' existe déjà.");
    }
}
