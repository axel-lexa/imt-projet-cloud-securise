package com.imt.vehicle.validators;

import com.imt.common.exceptions.BadRequestException;
import com.imt.common.exceptions.ImtException;
import com.imt.common.validators.AbstractValidatorStep;
import com.imt.vehicle.model.Vehicle;
import com.imt.vehicle.model.VehicleStateEnum;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class VehicleStateValidatorStep extends AbstractValidatorStep<Vehicle> {

    @Override
    public void check(final Vehicle toValidate) throws ImtException {
        if (VehicleStateEnum.UNKNOWN.equals(toValidate.getState())) {
            throw new BadRequestException(
                    String.format("L'état communiqué ne correspond pas aux valeurs acceptables : %s",
                            VehicleStateEnum.ACCEPTABLE_VALUES)
            );
        }
    }
}

