package com.imt.vehicle;

import com.imt.vehicle.model.Vehicle;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface VehicleStorageProvider {
    boolean exist(final String id);
    Collection<Vehicle> getAll();
    Optional<Vehicle> get(final String id);
    Optional<Vehicle> getByLicensePlate(final String licensePlate);
    Vehicle save(final Vehicle vehicle);
    void delete(final String id);
}
