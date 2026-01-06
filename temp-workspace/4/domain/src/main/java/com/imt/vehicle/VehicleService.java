package com.imt.vehicle;

import com.imt.common.exceptions.ImtException;
import com.imt.vehicle.model.Vehicle;
import lombok.AllArgsConstructor;
import java.util.*;

@AllArgsConstructor
public class VehicleService {

    protected VehicleStorageProvider service;

    public Collection<Vehicle> getAll() {
        return Objects.requireNonNullElse(this.service.getAll(), Collections.emptySet());
    }

    public Optional<Vehicle> getOne(final String id) {
        return this.service.get(id);
    }

    public Optional<Vehicle> getByLicensePlate(final String licensePlate) {
        return this.service.getByLicensePlate(licensePlate);
    }

    public Vehicle create(final Vehicle newVehicle) throws ImtException {
        return this.service.save(newVehicle);
    }

    public void update(final Vehicle updatedVehicle) throws ImtException {
        this.service.save(updatedVehicle);
    }

    public void delete(final String identifier) throws ImtException {
        this.service.delete(identifier);
    }
}
