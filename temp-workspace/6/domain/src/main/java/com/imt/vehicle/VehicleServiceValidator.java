package com.imt.vehicle;

import com.imt.common.exceptions.ImtException;
import com.imt.vehicle.model.Vehicle;
import com.imt.common.validators.ConstraintValidatorStep;
import com.imt.vehicle.validators.VehicleAlreadyExistValidatorStep;
import com.imt.vehicle.validators.VehicleEngineTypeValidatorStep;
import com.imt.vehicle.validators.VehicleStateValidatorStep;

public class VehicleServiceValidator extends VehicleService {

    public VehicleServiceValidator(final VehicleStorageProvider service) {
        super(service);
    }

    public Vehicle create(final Vehicle newVehicle) throws ImtException {
        new ConstraintValidatorStep<Vehicle>()
                .linkWith(new VehicleAlreadyExistValidatorStep(this.service))
                .linkWith(new VehicleStateValidatorStep())
                .linkWith(new VehicleEngineTypeValidatorStep())
                .validate(newVehicle)
                .throwIfInvalid();

        return super.create(newVehicle);
    }

    public void update(final Vehicle updatedVehicle) throws ImtException {
        new ConstraintValidatorStep<Vehicle>()
                .linkWith(new VehicleStateValidatorStep())
                .linkWith(new VehicleEngineTypeValidatorStep())
                .validate(updatedVehicle)
                .throwIfInvalid();

        super.update(updatedVehicle);
    }
}
