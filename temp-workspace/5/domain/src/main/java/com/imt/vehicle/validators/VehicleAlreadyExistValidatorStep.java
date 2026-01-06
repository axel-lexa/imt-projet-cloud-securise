package com.imt.vehicle.validators;

import com.imt.common.exceptions.ConflictException;
import com.imt.common.exceptions.ImtException;
import com.imt.common.validators.AbstractValidatorStep;
import com.imt.vehicle.VehicleStorageProvider;
import com.imt.vehicle.model.Vehicle;
import lombok.AllArgsConstructor;

import java.util.Optional;

@AllArgsConstructor
public class VehicleAlreadyExistValidatorStep extends AbstractValidatorStep<Vehicle> {

    protected VehicleStorageProvider service;

    @Override
    public void check(final Vehicle toValidate) throws ImtException {
        Optional<Vehicle> existingVehicleOpt = this.service.getByLicensePlate(toValidate.getLicensePlate());
        if (existingVehicleOpt.isPresent()) {
            Vehicle existingVehicle = existingVehicleOpt.get();
            if (!existingVehicle.getId().equals(toValidate.getId())) {
                throw new ConflictException(
                        String.format("Un véhicule avec la plaque d'immatriculation '%s' existe déjà.", toValidate.getLicensePlate())
                );
            }
        }
    }
}
