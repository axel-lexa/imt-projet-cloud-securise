package com.imt.adaptersinrest.contracts.model.input;

import com.imt.adaptersinrest.common.model.input.AbstractUpdateInput;
import com.imt.adaptersinrest.common.model.input.UpdatableProperty;
import com.imt.contracts.model.Contract;
import com.imt.contracts.model.ContractStateEnum;
import com.imt.vehicle.model.VehicleStateEnum;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serial;
import java.time.LocalDate;

/**
 * DTO d'entrée pour la mise à jour partielle (PATCH) d'un contrat.
 * On autorise ici la mise à jour des dates, de l'état du contrat et de l'état du véhicule
 * (par exemple lors d'une clôture ou annulation).
 */
@Getter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString
public class ContractUpdateInput extends AbstractUpdateInput {

    @Serial
    private static final long serialVersionUID = 1L;

    private UpdatableProperty<LocalDate> startDate = UpdatableProperty.empty();
    private UpdatableProperty<LocalDate> endDate = UpdatableProperty.empty();
    private UpdatableProperty<ContractStateEnum> state = UpdatableProperty.empty();
    private UpdatableProperty<VehicleStateEnum> vehicleState = UpdatableProperty.empty();

    /**
     * Conversion PATCH DTO -> Domaine.
     */
    public static Contract from(final ContractUpdateInput input, final Contract existingContract) {
        return existingContract.toBuilder()
                .startDate(input.getStartDate().defaultIfNotOverwrite(existingContract.getStartDate()))
                .endDate(input.getEndDate().defaultIfNotOverwrite(existingContract.getEndDate()))
                .state(input.getState().defaultIfNotOverwrite(existingContract.getState()))
                .vehicleState(input.getVehicleState().defaultIfNotOverwrite(existingContract.getVehicleState()))
                .build();
    }
}


