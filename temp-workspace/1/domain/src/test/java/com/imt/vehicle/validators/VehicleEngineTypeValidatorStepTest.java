package com.imt.vehicle.validators;

import com.imt.common.exceptions.BadRequestException;
import com.imt.vehicle.model.EngineTypeEnum;
import com.imt.vehicle.model.Vehicle;
import com.imt.vehicle.model.VehicleStateEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class VehicleEngineTypeValidatorStepTest {

    private VehicleEngineTypeValidatorStep validator;
    private Vehicle.VehicleBuilder vehicleBuilder;

    @BeforeEach
    void setUp() {
        validator = new VehicleEngineTypeValidatorStep();
        vehicleBuilder = Vehicle.builder()
                .brand("Test")
                .model("Test")
                .licensePlate("AA-123-BB")
                .color("Blue")
                .acquisitionDate(LocalDate.now())
                .state(VehicleStateEnum.AVAILABLE);
    }

    @Test
    void check_shouldThrowBadRequestException_whenEngineTypeIsUnknown() {
        // Given
        Vehicle vehicle = vehicleBuilder.engineType(EngineTypeEnum.UNKNOWN).build();

        // When & Then
        assertThatThrownBy(() -> validator.check(vehicle))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("La motorisation communiquée ne correspond pas aux valeurs acceptables");
    }

    @Test
    void check_shouldDoNothing_whenEngineTypeIsValid() {
        // Given
        Vehicle vehicle = vehicleBuilder.engineType(EngineTypeEnum.GASOLINE).build();

        // When & Then
        assertDoesNotThrow(() -> validator.check(vehicle));
    }
}
