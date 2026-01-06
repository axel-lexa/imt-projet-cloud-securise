package com.imt.adaptersoutbdd.vehicle.repositories.mappers;

import com.imt.adaptersoutbdd.common.model.mappers.AbstractBddMapper;
import com.imt.adaptersoutbdd.vehicle.repositories.entities.VehicleEntity;
import com.imt.vehicle.model.Vehicle;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class VehicleBddMapper extends AbstractBddMapper<Vehicle, VehicleEntity> {
    @Override
    public Vehicle from(VehicleEntity input) {
        if (input == null) return null;

        return Vehicle.builder()
                .id(input.getId())
                .licensePlate(input.getLicensePlate())
                .brand(input.getBrand())
                .model(input.getModel())
                .engineType(input.getEngineType())
                .color(input.getColor())
                .acquisitionDate(input.getAcquisitionDate())
                .state(input.getState())
                .build();
    }

    @Override
    public VehicleEntity to(Vehicle object) {
        if (object == null) return null;

        return VehicleEntity.builder()
                .id(object.getId().toString())
                .licensePlate(object.getLicensePlate())
                .brand(object.getBrand())
                .model(object.getModel())
                .engineType(object.getEngineType())
                .color(object.getColor())
                .acquisitionDate(object.getAcquisitionDate())
                .state(object.getState())
                .build();
    }
}
