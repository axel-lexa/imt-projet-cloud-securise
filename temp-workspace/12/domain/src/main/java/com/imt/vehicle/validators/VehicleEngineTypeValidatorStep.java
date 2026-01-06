package com.imt.vehicle.validators;

import com.imt.common.exceptions.BadRequestException;
import com.imt.common.exceptions.ImtException;
import com.imt.common.validators.AbstractValidatorStep;
import com.imt.vehicle.model.EngineTypeEnum;
import com.imt.vehicle.model.Vehicle;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class VehicleEngineTypeValidatorStep extends AbstractValidatorStep<Vehicle> {

    @Override
    public void check(final Vehicle toValidate) throws ImtException {
        if (EngineTypeEnum.UNKNOWN.equals(toValidate.getEngineType())) {
            throw new BadRequestException(
                    String.format("La motorisation communiquée ne correspond pas aux valeurs acceptables : %s",
                            EngineTypeEnum.ACCEPTABLE_VALUES)
            );
        }
    }
}

