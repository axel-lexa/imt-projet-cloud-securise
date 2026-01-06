package com.imt.adaptersinrest.contracts.model.output;

import com.imt.adaptersinrest.common.model.output.AbstractOutput;
import com.imt.contracts.model.Contract;
import com.imt.contracts.model.ContractStateEnum;
import com.imt.vehicle.model.VehicleStateEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.Serial;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO de sortie pour l'exposition d'un contrat via l'API REST.
 */
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString
public class ContractOutput extends AbstractOutput {

    @Serial
    private static final long serialVersionUID = 1L;

    private final UUID identifier;
    private final UUID clientIdentifier;
    private final UUID vehicleIdentifier;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final ContractStateEnum state;
    private final VehicleStateEnum vehicleState;

    /**
     * Conversion Domaine -> DTO.
     */
    public static ContractOutput from(final Contract contract) {
        return ContractOutput.builder()
                .identifier(contract.getIdentifier())
                .clientIdentifier(contract.getClientIdentifier())
                .vehicleIdentifier(contract.getVehicleIdentifier())
                .startDate(contract.getStartDate())
                .endDate(contract.getEndDate())
                .state(contract.getState())
                .vehicleState(contract.getVehicleState())
                .build();
    }
}


