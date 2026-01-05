package com.imt.contracts.validators;

import com.imt.common.exceptions.BadRequestException;
import com.imt.common.exceptions.ImtException;
import com.imt.common.validators.AbstractValidatorStep;
import com.imt.contracts.model.Contract;
import com.imt.vehicle.model.VehicleStateEnum;

public class ContractVehicleReadinessValidatorStep extends AbstractValidatorStep<Contract> {

    @Override
    public void check(final Contract toValidate) throws ImtException {
        final VehicleStateEnum vehicleState = toValidate.getVehicleState();

        if (VehicleStateEnum.BROKEN.equals(vehicleState)) {
            throw new BadRequestException("Les véhicules déclarés en panne ne peuvent pas être loués.");
        }

        if (VehicleStateEnum.UNKNOWN.equals(vehicleState)) {
            throw new BadRequestException(
                    String.format(
                            "L'état du véhicule doit être défini avant de créer un contrat : %s",
                            VehicleStateEnum.ACCEPTABLE_VALUES
                    )
            );
        }
    }
}