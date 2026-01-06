package com.imt.adaptersinrest.vehicle.model.output;

import com.imt.adaptersinrest.common.model.output.AbstractOutput;

import com.imt.vehicle.model.EngineTypeEnum;
import com.imt.vehicle.model.Vehicle;
import com.imt.vehicle.model.VehicleStateEnum;
import lombok.*;

import java.io.Serial;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString
public class VehicleOutput extends AbstractOutput {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String identifier;
    private final String brand;
    private final String model;
    private final EngineTypeEnum engineType;
    private final String color;
    private final String licensePlate;
    private final LocalDate acquisitionDate;
    private final VehicleStateEnum state;

    // Entity --> DTO
    public static VehicleOutput from(final Vehicle vehicle) {
        return VehicleOutput.builder()
                .identifier(vehicle.getId())
                .brand(vehicle.getBrand())
                .model(vehicle.getModel())
                .engineType(vehicle.getEngineType())
                .color(vehicle.getColor())
                .licensePlate(vehicle.getLicensePlate())
                .acquisitionDate(vehicle.getAcquisitionDate())
                .state(vehicle.getState())
                .build();
    }
}

