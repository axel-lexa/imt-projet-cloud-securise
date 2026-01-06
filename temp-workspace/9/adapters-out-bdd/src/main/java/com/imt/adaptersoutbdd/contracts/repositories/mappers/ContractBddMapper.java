package com.imt.adaptersoutbdd.contracts.repositories.mappers;

import com.imt.adaptersoutbdd.common.model.mappers.AbstractBddMapper;
import com.imt.adaptersoutbdd.contracts.repositories.entities.ContractEntity;
import com.imt.contracts.model.Contract;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class ContractBddMapper extends AbstractBddMapper<Contract, ContractEntity> {

    @Override
    public Contract from(ContractEntity input) {
        if (input == null) {
            return null;
        }

        return Contract.builder()
                .identifier(Optional.ofNullable(input.getId()).map(UUID::fromString).orElse(null))
                .clientIdentifier(Optional.ofNullable(input.getClientId()).map(UUID::fromString).orElse(null))
                .vehicleIdentifier(Optional.ofNullable(input.getVehicleId()).map(UUID::fromString).orElse(null))
                .startDate(input.getStartDate())
                .endDate(input.getEndDate())
                .state(input.getState())
                .vehicleState(input.getVehicleState())
                .build();
    }

    @Override
    public ContractEntity to(Contract object) {
        if (object == null) {
            return null;
        }

        return ContractEntity.builder()
                .id(Optional.ofNullable(object.getIdentifier()).map(UUID::toString).orElse(null))
                .clientId(Optional.ofNullable(object.getClientIdentifier()).map(UUID::toString).orElse(null))
                .vehicleId(Optional.ofNullable(object.getVehicleIdentifier()).map(UUID::toString).orElse(null))
                .startDate(object.getStartDate())
                .endDate(object.getEndDate())
                .state(object.getState())
                .vehicleState(object.getVehicleState())
                .build();
    }
}


