package com.imt.adaptersinrest.contracts.model.input;

import com.imt.adaptersinrest.common.model.input.AbstractInput;
import com.imt.contracts.model.Contract;
import com.imt.contracts.model.ContractStateEnum;
import com.imt.vehicle.model.VehicleStateEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serial;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO d'entrée pour la création (POST) d'un contrat.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString
public class ContractInput extends AbstractInput {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "L'identifiant du client ne peut pas être nul")
    private UUID clientIdentifier;

    @NotNull(message = "L'identifiant du véhicule ne peut pas être nul")
    private UUID vehicleIdentifier;

    @NotNull(message = "La date de début ne peut pas être nulle")
    private LocalDate startDate;

    @NotNull(message = "La date de fin ne peut pas être nulle")
    private LocalDate endDate;

    @NotNull(message = "L'état du contrat ne peut pas être nul")
    private ContractStateEnum state;

    @NotNull(message = "L'état du véhicule ne peut pas être nul")
    private VehicleStateEnum vehicleState;

    /**
     * Conversion DTO -> Domaine.
     */
    public static Contract convert(final ContractInput input) {
        return Contract.builder()
                .clientIdentifier(input.getClientIdentifier())
                .vehicleIdentifier(input.getVehicleIdentifier())
                .startDate(input.getStartDate())
                .endDate(input.getEndDate())
                .state(input.getState())
                .vehicleState(input.getVehicleState())
                .build();
    }
}


