package com.imt.contracts.model;

import com.imt.vehicle.model.VehicleStateEnum;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class Contract {

    @Builder.Default
    @NotNull(message = "L'identifiant du contrat ne peut pas être nul")
    private final UUID identifier = UUID.randomUUID();

    @NotNull(message = "L'identifiant du client ne peut pas être nul")
    private final UUID clientIdentifier;

    @NotNull(message = "L'identifiant du véhicule ne peut pas être nul")
    private final UUID vehicleIdentifier;

    @NotNull(message = "La date de début ne peut pas être nulle")
    private final LocalDate startDate;

    @NotNull(message = "La date de fin ne peut pas être nulle")
    private final LocalDate endDate;

    @NotNull(message = "L'état du contrat ne peut pas être nul")
    private final ContractStateEnum state;

    @NotNull(message = "L'état du véhicule ne peut pas être nul")
    private final VehicleStateEnum vehicleState;
}


