package com.imt.adaptersoutbdd.vehicle.repositories.mappers;

import com.imt.adaptersoutbdd.vehicle.repositories.entities.VehicleEntity;
import com.imt.vehicle.model.EngineTypeEnum;
import com.imt.vehicle.model.Vehicle;
import com.imt.vehicle.model.VehicleStateEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("VehicleBddMapper - Tests unitaires")
class VehicleBddMapperTest {

    private VehicleBddMapper vehicleMapper;

    @BeforeEach
    void setUp() {
        vehicleMapper = new VehicleBddMapper();
    }

    @Test
    @DisplayName("from() - Doit convertir Entity vers Domain")
    void from_shouldConvertEntityToDomain() {
        // Given
        LocalDate acquisitionDate = LocalDate.of(2023, 1, 1);
        VehicleEntity entity = VehicleEntity.builder()
                .id("mongo-id-123")
                .licensePlate("AA-123-BB")
                .brand("Renault")
                .model("Clio")
                .engineType(EngineTypeEnum.GASOLINE)
                .color("Blue")
                .acquisitionDate(acquisitionDate)
                .state(VehicleStateEnum.AVAILABLE)
                .build();

        // When
        Vehicle result = vehicleMapper.from(entity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("mongo-id-123");
        assertThat(result.getLicensePlate()).isEqualTo("AA-123-BB");
        assertThat(result.getBrand()).isEqualTo("Renault");
        assertThat(result.getModel()).isEqualTo("Clio");
        assertThat(result.getEngineType()).isEqualTo(EngineTypeEnum.GASOLINE);
        assertThat(result.getColor()).isEqualTo("Blue");
        assertThat(result.getAcquisitionDate()).isEqualTo(acquisitionDate);
        assertThat(result.getState()).isEqualTo(VehicleStateEnum.AVAILABLE);
    }

    @Test
    @DisplayName("from() - Doit retourner null si l'entrée est null")
    void from_shouldReturnNull_whenInputIsNull() {
        // When & Then
        assertThat(vehicleMapper.from((VehicleEntity) null)).isNull();
    }

    @Test
    @DisplayName("to() - Doit convertir Domain vers Entity")
    void to_shouldConvertDomainToEntity() {
        // Given
        LocalDate acquisitionDate = LocalDate.of(2022, 5, 20);
        Vehicle vehicle = Vehicle.builder()
                .id("domain-id-456")
                .licensePlate("CC-456-DD")
                .brand("Peugeot")
                .model("208")
                .engineType(EngineTypeEnum.DIESEL)
                .color("Red")
                .acquisitionDate(acquisitionDate)
                .state(VehicleStateEnum.IN_RENTAL)
                .build();

        // When
        VehicleEntity result = vehicleMapper.to(vehicle);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("domain-id-456");
        assertThat(result.getLicensePlate()).isEqualTo("CC-456-DD");
        assertThat(result.getBrand()).isEqualTo("Peugeot");
        assertThat(result.getModel()).isEqualTo("208");
        assertThat(result.getEngineType()).isEqualTo(EngineTypeEnum.DIESEL);
        assertThat(result.getColor()).isEqualTo("Red");
        assertThat(result.getAcquisitionDate()).isEqualTo(acquisitionDate);
        assertThat(result.getState()).isEqualTo(VehicleStateEnum.IN_RENTAL);
    }

    @Test
    @DisplayName("to() - Doit retourner null si l'entrée est null")
    void to_shouldReturnNull_whenInputIsNull() {
        // When & Then
        assertThat(vehicleMapper.to(null)).isNull();
    }
}
